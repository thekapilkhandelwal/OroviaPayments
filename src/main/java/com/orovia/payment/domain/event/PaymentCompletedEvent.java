package com.orovia.payment.domain.event;

import lombok.Value;

/**
 * Domain event emitted after payment completion.
 */
@Value
public class PaymentCompletedEvent {
    Long paymentOrderId;
    Long bookingId;
    String currency;
}
