package com.orovia.payment.controller;

import com.orovia.payment.domain.model.LedgerReferenceType;
import com.orovia.payment.dto.LedgerEntryResponse;
import com.orovia.payment.mapper.LedgerMapper;
import com.orovia.payment.service.LedgerService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Ledger endpoints for auditability.
 */
@RestController
@RequestMapping("/api/v1/ledger")
public class LedgerController {

    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    /**
     * Fetch ledger entries by reference.
     *
     * @param referenceType type
     * @param referenceId id
     * @return list
     */
    @GetMapping("/entries")
    public ResponseEntity<List<LedgerEntryResponse>> list(@RequestParam String referenceType,
                                                          @RequestParam Long referenceId) {
        List<LedgerEntryResponse> responses = ledgerService
                .findByReference(LedgerReferenceType.valueOf(referenceType), referenceId)
                .stream().map(LedgerMapper::toResponse).toList();
        return ResponseEntity.ok(responses);
    }
}
