package com.orovia.payment.service.job;

import com.orovia.payment.service.ReconciliationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled job running reconciliation daily.
 */
@Component
@RequiredArgsConstructor
public class PaymentReconciliationJob {

    private static final Logger log = LoggerFactory.getLogger(PaymentReconciliationJob.class);
    private final ReconciliationService reconciliationService;

    /**
     * Execute reconciliation.
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void run() {
        log.info("Starting reconciliation job");
        reconciliationService.reconcile();
    }
}
