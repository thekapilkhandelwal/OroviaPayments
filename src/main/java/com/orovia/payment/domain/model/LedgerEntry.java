package com.orovia.payment.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Double entry ledger record capturing fund movements.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {

    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    private LedgerReferenceType referenceType;

    private Long referenceId;
    private String debitAccount;
    private String creditAccount;
    private BigDecimal amount;
    private String currency;
    private String narration;
    private OffsetDateTime createdAt;
}
