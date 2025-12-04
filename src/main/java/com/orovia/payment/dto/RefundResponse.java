package com.orovia.payment.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

/**
 * Response body for refund creation and queries.
 */
@Data
@Builder
public class RefundResponse {
    private Long id;
    private Long paymentOrderId;
    private BigDecimal amount;
    private String status;
    private String reason;
    private String externalRefundId;
}
