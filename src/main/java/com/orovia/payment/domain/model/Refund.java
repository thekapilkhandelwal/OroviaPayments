package com.orovia.payment.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Refund represents a return of funds back to the customer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "refunds")
public class Refund {

    @Id
    private Long id;

    private Long paymentOrderId;
    private BigDecimal amount;
    private String reason;

    @Enumerated(EnumType.STRING)
    private RefundStatus status;

    private String externalPgRefundId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
