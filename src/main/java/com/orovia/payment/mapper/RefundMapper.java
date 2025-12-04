package com.orovia.payment.mapper;

import com.orovia.payment.domain.model.Refund;
import com.orovia.payment.dto.RefundResponse;

/**
 * Mapper converting refund entities to external representations.
 */
public class RefundMapper {

    private RefundMapper() {
    }

    /**
     * Build response from entity.
     *
     * @param refund refund entity
     * @return response
     */
    public static RefundResponse toResponse(Refund refund) {
        return RefundResponse.builder()
                .id(refund.getId())
                .paymentOrderId(refund.getPaymentOrderId())
                .amount(refund.getAmount())
                .status(refund.getStatus().name())
                .reason(refund.getReason())
                .externalRefundId(refund.getExternalPgRefundId())
                .build();
    }
}
