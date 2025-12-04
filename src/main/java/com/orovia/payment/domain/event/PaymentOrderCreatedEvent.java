package com.orovia.payment.domain.event;

import java.math.BigDecimal;
import lombok.Value;

@Value
public class PaymentOrderCreatedEvent {
    Long paymentOrderId;
    Long bookingId;
    BigDecimal amount;
    String currency;
    String paymentMethod;
}

