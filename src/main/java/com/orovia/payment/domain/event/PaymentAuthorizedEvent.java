package com.orovia.payment.domain.event;

import java.math.BigDecimal;
import lombok.Value;

@Value
public class PaymentAuthorizedEvent {
    Long paymentOrderId;
    Long bookingId;
    BigDecimal amount;
    String currency;
    String paymentMethod;
}

