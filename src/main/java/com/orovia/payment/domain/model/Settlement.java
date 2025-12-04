package com.orovia.payment.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Settlement batches payouts to hotels.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "settlements")
public class Settlement {

    @Id
    private Long id;

    private Long hotelId;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private SettlementStatus status;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String externalPayoutReference;
}
