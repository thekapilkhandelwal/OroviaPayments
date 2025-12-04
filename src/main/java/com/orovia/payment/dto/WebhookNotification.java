package com.orovia.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Generic webhook payload from gateway.
 */
@Data
public class WebhookNotification {
    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("payment_id")
    private String paymentId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("signature")
    private String signature;

    @JsonProperty("amount")
    private String amount;
}
