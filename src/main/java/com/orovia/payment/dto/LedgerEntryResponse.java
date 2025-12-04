package com.orovia.payment.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

/**
 * DTO representing ledger entries for API exposure.
 */
@Data
@Builder
public class LedgerEntryResponse {
    private Long id;
    private String referenceType;
    private Long referenceId;
    private String debitAccount;
    private String creditAccount;
    private BigDecimal amount;
    private String currency;
    private String narration;
}
