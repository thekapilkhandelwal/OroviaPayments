package com.orovia.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

/**
 * Request payload for creating payment orders.
 */
@Data
public class PaymentOrderRequest {
    @NotNull
    private Long bookingId;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    @NotBlank
    private String currency;

    @NotBlank
    private String paymentMethod;

    private String returnUrl;

    @NotNull
    private Long customerId;

    @NotNull
    private Long hotelId;
}
