package com.orovia.payment.service;

import com.orovia.payment.domain.model.PaymentOrder;
import com.orovia.payment.integration.PaymentGatewayRouter;
import com.orovia.payment.repository.PaymentOrderRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service for actively querying gateway status.
 */
@Service
@RequiredArgsConstructor
public class PaymentStatusService {

    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentGatewayRouter paymentGatewayRouter;

    /**
     * Query gateway and update status.
     *
     * @param paymentOrderId id of order
     * @return updated order or empty when missing
     */
    public Optional<PaymentOrder> refreshStatus(Long paymentOrderId) {
        return paymentOrderRepository.findById(paymentOrderId).map(order -> {
            String status = paymentGatewayRouter.resolve(order.getPaymentMethod())
                    .fetchPaymentStatus(order.getExternalPgOrderId());
            if ("SUCCESS".equalsIgnoreCase(status)) {
                order.setStatus(com.orovia.payment.domain.model.PaymentOrderStatus.SUCCESS);
            }
            order.setUpdatedAt(java.time.OffsetDateTime.now());
            return paymentOrderRepository.save(order);
        });
    }
}
