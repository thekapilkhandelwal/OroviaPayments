package com.orovia.payment.mapper;

import com.orovia.payment.domain.model.LedgerEntry;
import com.orovia.payment.dto.LedgerEntryResponse;

/**
 * Mapper for ledger entries.
 */
public class LedgerMapper {
    private LedgerMapper() {
    }

    /**
     * Maps ledger entity to DTO.
     *
     * @param entry ledger entry
     * @return response DTO
     */
    public static LedgerEntryResponse toResponse(LedgerEntry entry) {
        return LedgerEntryResponse.builder()
                .id(entry.getId())
                .referenceType(entry.getReferenceType().name())
                .referenceId(entry.getReferenceId())
                .debitAccount(entry.getDebitAccount())
                .creditAccount(entry.getCreditAccount())
                .amount(entry.getAmount())
                .currency(entry.getCurrency())
                .narration(entry.getNarration())
                .build();
    }
}
