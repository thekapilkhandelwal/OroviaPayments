package com.orovia.payment.domain.event;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ledger update notification for downstream reconciliation and settlements.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LedgerUpdatedEvent {
    private Long entryId;
    private Long referenceId;
    private String referenceType;
    private BigDecimal amount;
    private String currency;
}
