package com.orovia.payment.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * DTO for settlement details.
 */
@Data
@Builder
public class SettlementResponse {
    private Long id;
    private Long hotelId;
    private BigDecimal amount;
    private String status;
    private OffsetDateTime createdAt;
}
