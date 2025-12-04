package com.orovia.payment.service;

import com.orovia.payment.domain.event.RefundCompletedEvent;
import com.orovia.payment.domain.event.RefundCreatedEvent;
import com.orovia.payment.domain.model.LedgerReferenceType;
import com.orovia.payment.domain.model.PaymentOrder;
import com.orovia.payment.domain.model.PaymentOrderStatus;
import com.orovia.payment.domain.model.Refund;
import com.orovia.payment.domain.model.RefundStatus;
import com.orovia.payment.dto.RefundRequest;
import com.orovia.payment.dto.RefundResponse;
import com.orovia.payment.exception.BusinessException;
import com.orovia.payment.exception.ResourceNotFoundException;
import com.orovia.payment.integration.PaymentGatewayClient;
import com.orovia.payment.integration.PaymentGatewayRouter;
import com.orovia.payment.mapper.RefundMapper;
import com.orovia.payment.event.DomainEventPublisher;
import com.orovia.payment.repository.PaymentOrderRepository;
import com.orovia.payment.repository.RefundRepository;
import com.orovia.payment.shared.id.IdGenerator;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service handling refund lifecycle.
 */
@Service
@RequiredArgsConstructor
public class RefundService {

    private final RefundRepository refundRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentGatewayRouter paymentGatewayRouter;
    private final LedgerService ledgerService;
    private final DomainEventPublisher eventPublisher;
    private final IdGenerator idGenerator;

    /**
     * Issue a refund for a payment order.
     *
     * @param paymentOrderId payment order id
     * @param request refund request
     * @return response dto
     */
    @Transactional
    public RefundResponse createRefund(Long paymentOrderId, RefundRequest request) {
        PaymentOrder paymentOrder = paymentOrderRepository.findById(paymentOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment order not found"));
        if (paymentOrder.getStatus() != PaymentOrderStatus.SUCCESS) {
            throw new BusinessException("INVALID_STATE", "Refund only allowed for successful payments");
        }
        if (request.getAmount().compareTo(paymentOrder.getAmount()) > 0) {
            throw new BusinessException("INVALID_AMOUNT", "Refund exceeds paid amount");
        }

        Refund refund = Refund.builder()
                .id(idGenerator.nextId())
                .paymentOrderId(paymentOrderId)
                .amount(request.getAmount())
                .reason(request.getReason())
                .status(RefundStatus.PENDING)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        Refund saved = refundRepository.save(refund);
        eventPublisher.publish("refunds.created", new RefundCreatedEvent(saved.getId(), paymentOrderId,
                saved.getAmount(), paymentOrder.getCurrency()));

        PaymentGatewayClient client = paymentGatewayRouter.resolve(paymentOrder.getPaymentMethod());
        String externalRefundId = client.refundPayment(paymentOrder.getExternalPgPaymentId(), request.getAmount());
        saved.setExternalPgRefundId(externalRefundId);
        saved.setStatus(RefundStatus.SUCCESS);
        saved.setUpdatedAt(OffsetDateTime.now());
        refundRepository.save(saved);
        eventPublisher.publish("refunds.completed", new RefundCompletedEvent(saved.getId(), paymentOrderId,
                saved.getAmount(), paymentOrder.getCurrency(), saved.getStatus().name()));

        ledgerService.record(LedgerReferenceType.REFUND, paymentOrderId, "orovia:platform",
                "customer:" + paymentOrder.getBookingId(), request.getAmount(), paymentOrder.getCurrency(), "Refund processed");
        paymentOrder.setStatus(PaymentOrderStatus.REFUNDED);
        paymentOrderRepository.save(paymentOrder);
        return RefundMapper.toResponse(saved);
    }

    /**
     * Retrieve refunds for payment order.
     *
     * @param paymentOrderId order id
     * @return list of responses
     */
    @Transactional(readOnly = true)
    public List<RefundResponse> getRefunds(Long paymentOrderId) {
        return refundRepository.findByPaymentOrderId(paymentOrderId).stream()
                .map(RefundMapper::toResponse)
                .toList();
    }
}
