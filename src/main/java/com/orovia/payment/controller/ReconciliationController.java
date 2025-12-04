package com.orovia.payment.controller;

import com.orovia.payment.dto.ReconciliationReportResponse;
import com.orovia.payment.service.ReconciliationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API exposing reconciliation results.
 */
@RestController
@RequestMapping("/api/v1/reconciliation")
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    public ReconciliationController(ReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    /**
     * Run reconciliation and return report.
     *
     * @return report
     */
    @GetMapping("/reports")
    public ResponseEntity<ReconciliationReportResponse> report() {
        return ResponseEntity.ok(reconciliationService.reconcile());
    }
}
