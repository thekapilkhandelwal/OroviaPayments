package com.orovia.payment.mapper;

import com.orovia.payment.domain.model.Settlement;
import com.orovia.payment.dto.SettlementResponse;

/**
 * Mapper for settlements.
 */
public class SettlementMapper {
    private SettlementMapper() {
    }

    /**
     * Map settlement to response.
     *
     * @param settlement settlement entity
     * @return response dto
     */
    public static SettlementResponse toResponse(Settlement settlement) {
        return SettlementResponse.builder()
                .id(settlement.getId())
                .hotelId(settlement.getHotelId())
                .amount(settlement.getAmount())
                .status(settlement.getStatus().name())
                .createdAt(settlement.getCreatedAt())
                .build();
    }
}
