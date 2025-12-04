package com.orovia.payment.repository;

import com.orovia.payment.domain.model.LedgerEntry;
import com.orovia.payment.domain.model.LedgerReferenceType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for ledger entries.
 */
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {
    List<LedgerEntry> findByReferenceTypeAndReferenceId(LedgerReferenceType referenceType, Long referenceId);

    List<LedgerEntry> findByReferenceType(LedgerReferenceType referenceType);
}
