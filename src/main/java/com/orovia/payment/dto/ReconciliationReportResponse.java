package com.orovia.payment.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * Response DTO for reconciliation results.
 */
@Data
@Builder
public class ReconciliationReportResponse {
    private LocalDate date;
    private List<String> mismatches;
}
