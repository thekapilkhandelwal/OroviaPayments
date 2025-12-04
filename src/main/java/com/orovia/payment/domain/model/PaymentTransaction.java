package com.orovia.payment.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents each payment attempt response from gateway.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payment_transactions")
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long paymentOrderId;
    private BigDecimal amount;
    private String currency;
    private String gatewayProvider;
    private String externalTransactionId;
    private String status;
    private String errorCode;
    private String errorMessage;
    private OffsetDateTime createdAt;
}
