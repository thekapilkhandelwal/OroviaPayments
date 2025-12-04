package com.orovia.payment.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

/**
 * Response payload describing a payment order.
 */
@Data
@Builder
public class PaymentOrderResponse {
    private Long id;
    private Long bookingId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String paymentMethod;
    private String externalOrderId;
    private String paymentUrl;
}
