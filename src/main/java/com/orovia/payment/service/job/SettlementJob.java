package com.orovia.payment.service.job;

import com.orovia.payment.domain.model.Settlement;
import com.orovia.payment.service.SettlementService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled job generating hotel settlements.
 */
@Component
@RequiredArgsConstructor
public class SettlementJob {

    private static final Logger log = LoggerFactory.getLogger(SettlementJob.class);
    private final SettlementService settlementService;

    /**
     * Create settlements daily and mark as paid for demo.
     */
    @Scheduled(cron = "0 30 2 * * *")
    public void run() {
        List<Settlement> settlements = settlementService.createPendingSettlements();
        settlements.forEach(settlementService::completeSettlement);
        log.info("Processed {} settlements", settlements.size());
    }
}
