package com.orovia.payment.domain.event;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event emitted when refund completes and ledger can be updated.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundCompletedEvent {
    private Long refundId;
    private Long paymentOrderId;
    private BigDecimal amount;
    private String currency;
    private String status;
}
