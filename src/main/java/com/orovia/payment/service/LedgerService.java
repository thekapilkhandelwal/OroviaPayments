package com.orovia.payment.service;

import com.orovia.payment.domain.event.LedgerUpdatedEvent;
import com.orovia.payment.domain.model.LedgerEntry;
import com.orovia.payment.domain.model.LedgerReferenceType;
import com.orovia.payment.event.DomainEventPublisher;
import com.orovia.payment.repository.LedgerEntryRepository;
import com.orovia.payment.shared.id.IdGenerator;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service managing double-entry ledger operations.
 */
@Service
@RequiredArgsConstructor
public class LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;
    private final IdGenerator idGenerator;
    private final DomainEventPublisher eventPublisher;

    /**
     * Persist a ledger entry.
     *
     * @param referenceType type of event
     * @param referenceId related entity id
     * @param debitAccount debit
     * @param creditAccount credit
     * @param amount amount
     * @param currency currency
     * @param narration description
     * @return saved entry
     */
    @Transactional
    public LedgerEntry record(LedgerReferenceType referenceType, Long referenceId, String debitAccount,
                              String creditAccount, BigDecimal amount, String currency, String narration) {
        LedgerEntry entry = LedgerEntry.builder()
                .id(idGenerator.nextId())
                .referenceType(referenceType)
                .referenceId(referenceId)
                .debitAccount(debitAccount)
                .creditAccount(creditAccount)
                .amount(amount)
                .currency(currency)
                .narration(narration)
                .createdAt(OffsetDateTime.now())
                .build();
        LedgerEntry saved = ledgerEntryRepository.save(entry);
        eventPublisher.publish("ledger.updated", new LedgerUpdatedEvent(saved.getId(), saved.getReferenceId(),
                saved.getReferenceType().name(), saved.getAmount(), saved.getCurrency()));
        return saved;
    }

    /**
     * Fetch ledger entries by reference.
     *
     * @param referenceType reference type
     * @param referenceId reference id
     * @return list
     */
    @Transactional(readOnly = true)
    public List<LedgerEntry> findByReference(LedgerReferenceType referenceType, Long referenceId) {
        return ledgerEntryRepository.findByReferenceTypeAndReferenceId(referenceType, referenceId);
    }

    /**
     * Fetch all entries by type.
     *
     * @param referenceType type
     * @return list
     */
    @Transactional(readOnly = true)
    public List<LedgerEntry> findByType(LedgerReferenceType referenceType) {
        return ledgerEntryRepository.findByReferenceType(referenceType);
    }
}
