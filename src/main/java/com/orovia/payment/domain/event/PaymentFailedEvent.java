package com.orovia.payment.domain.event;

import lombok.Value;

/**
 * Domain event emitted when payment fails at gateway.
 */
@Value
public class PaymentFailedEvent {
    Long paymentOrderId;
    Long bookingId;
    String reason;
}
