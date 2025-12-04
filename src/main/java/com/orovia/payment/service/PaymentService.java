package com.orovia.payment.service;

import com.orovia.payment.domain.event.PaymentAuthorizedEvent;
import com.orovia.payment.domain.event.PaymentOrderCreatedEvent;
import com.orovia.payment.domain.model.Booking;
import com.orovia.payment.domain.model.BookingStatus;
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
import com.orovia.payment.event.DomainEventPublisher;
import com.orovia.payment.repository.BookingRepository;
import com.orovia.payment.repository.PaymentOrderRepository;
import com.orovia.payment.repository.PaymentTransactionRepository;
import com.orovia.payment.shared.cache.CacheService;
import com.orovia.payment.shared.config.ScalingProperties;
import com.orovia.payment.shared.id.IdGenerator;
import com.orovia.payment.shared.ratelimit.RateLimiterService;
import com.orovia.payment.shared.shard.ShardRoutingService;
import java.time.OffsetDateTime;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final DomainEventPublisher eventPublisher;
    private final IdempotencyService idempotencyService;
    private final IdGenerator idGenerator;
    private final RateLimiterService rateLimiterService;
    private final CacheService cacheService;
    private final ScalingProperties scalingProperties;
    private final ShardRoutingService shardRoutingService;

    /**
     * Creates a payment order using idempotency.
     *
     * @param request create request
     * @return response
     */
    @Transactional
    public PaymentOrderResponse createPaymentOrder(PaymentOrderRequest request) {
        if (!rateLimiterService.tryConsume("api:create-payment")) {
            throw new BusinessException("RATE_LIMITED", "Payment order creation throttled");
        }
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

        if (!idempotencyService.acquire("payment:create:" + idempotencyKey, Duration.ofMinutes(10))) {
            log.info("Create payment order skipped due to idempotency key {}", idempotencyKey);
            return existingOrder.map(order -> PaymentMapper.toResponse(order, order.getReturnUrl()))
                    .orElseThrow(() -> new BusinessException("IDEMPOTENT", "Duplicate create request"));
        }

        long paymentId = idGenerator.nextId();
        PaymentOrder paymentOrder = PaymentMapper.toEntity(request, paymentId);
        paymentOrder.setIdempotencyKey(idempotencyKey);
        shardRoutingService.resolveShard(String.valueOf(paymentOrder.getBookingId()));
        PaymentGatewayClient client = paymentGatewayRouter.resolve(paymentOrder.getPaymentMethod());
        Map<String, String> gatewayResponse = client.createOrder(paymentOrder);
        paymentOrder.setExternalPgOrderId(gatewayResponse.get("externalOrderId"));
        paymentOrder.setStatus(PaymentOrderStatus.PENDING);
        paymentOrder.setUpdatedAt(OffsetDateTime.now());
        PaymentOrder persisted = paymentOrderRepository.save(paymentOrder);
        eventPublisher.publish("payments.created", new PaymentOrderCreatedEvent(persisted.getId(),
                persisted.getBookingId(), persisted.getAmount(), persisted.getCurrency(),
                persisted.getPaymentMethod().name()));
        cacheService.put(cacheKey(paymentOrder.getId()), PaymentMapper.toResponse(persisted, gatewayResponse.get("paymentUrl")),
                scalingProperties.getCache().getStatusTtlSeconds());
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
        if (!rateLimiterService.tryConsume("webhook:" + provider)) {
            throw new BusinessException("RATE_LIMITED", "Webhook flow throttled");
        }
        if (!idempotencyService.acquire("payment:webhook:" + provider + ":" + notification.getOrderId() + ":" +
                notification.getPaymentId(), Duration.ofMinutes(5))) {
            log.info("Webhook deduplicated for provider {} order {}", provider, notification.getOrderId());
            return paymentOrderRepository.findByExternalPgOrderId(notification.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Payment order not found"));
        }

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
                .id(idGenerator.nextId())
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
            eventPublisher.publish("payments.authorized", new PaymentAuthorizedEvent(order.getId(), order.getBookingId(),
                    order.getAmount(), order.getCurrency(), order.getPaymentMethod().name()));
        } else {
            order.setStatus(PaymentOrderStatus.FAILED);
        }
        PaymentOrder saved = paymentOrderRepository.save(order);
        cacheService.put(cacheKey(order.getId()), PaymentMapper.toResponse(saved, null),
                scalingProperties.getCache().getStatusTtlSeconds());
        return saved;
    }

    /**
     * Fetch payment order.
     *
     * @param id identifier
     * @return order
     */
    @Transactional(readOnly = true)
    public PaymentOrder getPaymentOrder(Long id) {
        return cacheService.get(cacheKey(id), PaymentOrderResponse.class)
                .map(response -> paymentOrderRepository.findById(response.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Payment order not found")))
                .orElseGet(() -> paymentOrderRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Payment order not found")));
    }

    private String cacheKey(Long id) {
        return "payment:status:" + id;
    }
}
