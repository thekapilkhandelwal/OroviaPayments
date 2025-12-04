package com.orovia.payment.service;

import com.orovia.payment.domain.event.PaymentCompletedEvent;
import com.orovia.payment.domain.model.Booking;
import com.orovia.payment.domain.model.BookingStatus;
import com.orovia.payment.domain.model.LedgerReferenceType;
import com.orovia.payment.domain.model.PaymentMethod;
import com.orovia.payment.domain.model.PaymentOrder;
import com.orovia.payment.domain.model.PaymentOrderStatus;
import com.orovia.payment.domain.model.PaymentTransaction;
import com.orovia.payment.dto.PaymentOrderRequest;
import com.orovia.payment.dto.PaymentOrderResponse;
import com.orovia.payment.dto.WebhookNotification;
import com.orovia.payment.exception.BusinessException;
import com.orovia.payment.exception.ResourceNotFoundException;
import com.orovia.payment.integration.PaymentGatewayClient;
import com.orovia.payment.integration.PaymentGatewayRouter;
import com.orovia.payment.mapper.PaymentMapper;
import com.orovia.payment.repository.BookingRepository;
import com.orovia.payment.repository.PaymentOrderRepository;
import com.orovia.payment.repository.PaymentTransactionRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service handling the payment lifecycle.
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentOrderRepository paymentOrderRepository;
    private final BookingRepository bookingRepository;
    private final PaymentGatewayRouter paymentGatewayRouter;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final LedgerService ledgerService;
    private final ApplicationEventPublisher eventPublisher;
    private final SettlementService settlementService;

    /**
     * Creates a payment order using idempotency.
     *
     * @param request create request
     * @return response
     */
    @Transactional
    public PaymentOrderResponse createPaymentOrder(PaymentOrderRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BusinessException("BOOKING_CANCELLED", "Booking is not payable");
        }

        String idempotencyKey = request.getBookingId() + "-" + request.getPaymentMethod();
        Optional<PaymentOrder> existingOrder = paymentOrderRepository.findByIdempotencyKey(idempotencyKey);
        if (existingOrder.isPresent()) {
            return PaymentMapper.toResponse(existingOrder.get(), existingOrder.get().getReturnUrl());
        }

        PaymentOrder paymentOrder = PaymentMapper.toEntity(request);
        paymentOrder.setIdempotencyKey(idempotencyKey);
        PaymentGatewayClient client = paymentGatewayRouter.resolve(paymentOrder.getPaymentMethod());
        Map<String, String> gatewayResponse = client.createOrder(paymentOrder);
        paymentOrder.setExternalPgOrderId(gatewayResponse.get("externalOrderId"));
        paymentOrder.setStatus(PaymentOrderStatus.PENDING);
        paymentOrder.setUpdatedAt(OffsetDateTime.now());
        PaymentOrder persisted = paymentOrderRepository.save(paymentOrder);
        return PaymentMapper.toResponse(persisted, gatewayResponse.get("paymentUrl"));
    }

    /**
     * Process webhook callback from providers.
     *
     * @param provider gateway identifier
     * @param notification payload
     * @param rawBody raw body for signature
     * @return updated payment order
     */
    @Transactional
    public PaymentOrder handleWebhook(String provider, WebhookNotification notification, String rawBody) {
        PaymentOrder order = paymentOrderRepository.findByExternalPgOrderId(notification.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment order not found"));

        PaymentGatewayClient client = paymentGatewayRouter.resolve(order.getPaymentMethod());
        if (!client.verifyWebhookSignature(rawBody, notification.getSignature())) {
            throw new BusinessException("SIGNATURE_INVALID", "Invalid webhook signature");
        }

        if (order.getStatus() == PaymentOrderStatus.SUCCESS) {
            log.info("Webhook is idempotent for order {}", order.getId());
            return order;
        }

        boolean success = "SUCCESS".equalsIgnoreCase(notification.getStatus());
        PaymentTransaction txn = PaymentTransaction.builder()
                .paymentOrderId(order.getId())
                .amount(order.getAmount())
                .currency(order.getCurrency())
                .gatewayProvider(provider)
                .externalTransactionId(notification.getPaymentId())
                .status(notification.getStatus())
                .createdAt(OffsetDateTime.now())
                .build();
        paymentTransactionRepository.save(txn);

        order.setExternalPgPaymentId(notification.getPaymentId());
        order.setUpdatedAt(OffsetDateTime.now());

        if (success) {
            order.setStatus(PaymentOrderStatus.SUCCESS);
            bookingRepository.findById(order.getBookingId()).ifPresent(booking -> {
                booking.setStatus(BookingStatus.CONFIRMED);
                booking.setUpdatedAt(OffsetDateTime.now());
                bookingRepository.save(booking);
            });
            ledgerService.record(LedgerReferenceType.PAYMENT, order.getId(), "customer:" + order.getBookingId(),
                    "orovia:platform", order.getAmount(), order.getCurrency(), "Payment captured");
            ledgerService.record(LedgerReferenceType.PAYMENT, order.getId(), "orovia:platform",
                    "hotel:" + order.getBookingId(), order.getAmount(), order.getCurrency(), "Hotel payable");
            settlementService.applySettlementSplit(order);
            eventPublisher.publishEvent(new PaymentCompletedEvent(order.getId(), order.getBookingId(), order.getCurrency()));
        } else {
            order.setStatus(PaymentOrderStatus.FAILED);
        }
        return paymentOrderRepository.save(order);
    }

    /**
     * Fetch payment order.
     *
     * @param id identifier
     * @return order
     */
    @Transactional(readOnly = true)
    public PaymentOrder getPaymentOrder(Long id) {
        return paymentOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment order not found"));
    }
}
