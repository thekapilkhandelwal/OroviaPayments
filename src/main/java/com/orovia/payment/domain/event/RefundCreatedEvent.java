package com.orovia.payment.domain.event;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event fired when a refund request is accepted.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundCreatedEvent {
    private Long refundId;
    private Long paymentOrderId;
    private BigDecimal amount;
    private String currency;
}
