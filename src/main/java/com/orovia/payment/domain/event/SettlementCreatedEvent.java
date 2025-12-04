package com.orovia.payment.domain.event;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Emitted when a settlement batch is generated for a hotel account.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SettlementCreatedEvent {
    private Long settlementId;
    private Long hotelAccountId;
    private BigDecimal amount;
    private String currency;
}
