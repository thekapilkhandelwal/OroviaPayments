package com.orovia.payment.service;

import com.orovia.payment.domain.model.PaymentOrder;
import com.orovia.payment.domain.model.PaymentTransaction;
import com.orovia.payment.dto.ReconciliationReportResponse;
import com.orovia.payment.integration.PaymentGatewayRouter;
import com.orovia.payment.repository.PaymentOrderRepository;
import com.orovia.payment.repository.PaymentTransactionRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service performing simplified reconciliation against gateway reports.
 */
@Service
@RequiredArgsConstructor
public class ReconciliationService {

    private static final Logger log = LoggerFactory.getLogger(ReconciliationService.class);

    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentGatewayRouter paymentGatewayRouter;

    /**
     * Runs reconciliation for all orders.
     *
     * @return report response
     */
    @Transactional(readOnly = true)
    public ReconciliationReportResponse reconcile() {
        List<String> mismatches = new ArrayList<>();
        for (PaymentOrder order : paymentOrderRepository.findAll()) {
            String gatewayStatus = paymentGatewayRouter.resolve(order.getPaymentMethod())
                    .fetchPaymentStatus(order.getExternalPgOrderId());
            if (!gatewayStatus.equalsIgnoreCase(order.getStatus().name())) {
                mismatches.add("Order " + order.getId() + " status mismatch: local=" + order.getStatus() + " gateway=" + gatewayStatus);
            }
            List<PaymentTransaction> txns = paymentTransactionRepository.findByPaymentOrderId(order.getId());
            if (txns.isEmpty()) {
                mismatches.add("Order " + order.getId() + " has no transaction recorded");
            }
        }
        log.info("Reconciliation complete with {} mismatches", mismatches.size());
        return ReconciliationReportResponse.builder().date(LocalDate.now()).mismatches(mismatches).build();
    }
}
