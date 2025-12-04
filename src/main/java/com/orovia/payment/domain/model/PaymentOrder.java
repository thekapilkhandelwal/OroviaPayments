package com.orovia.payment.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payment order links a booking to a payment gateway order lifecycle.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payment_orders", indexes = {
        @Index(name = "idx_payment_orders_booking", columnList = "bookingId"),
        @Index(name = "idx_payment_orders_idempotency", columnList = "idempotencyKey", unique = true)
})
public class PaymentOrder {

    @Id
    private Long id;

    private Long bookingId;
    private BigDecimal amount;
    private String currency;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private PaymentOrderStatus status;

    private String externalPgOrderId;
    private String externalPgPaymentId;
    private String idempotencyKey;
    private String returnUrl;

    private String type;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
