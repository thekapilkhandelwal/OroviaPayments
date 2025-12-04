package com.orovia.payment.mapper;

import com.orovia.payment.domain.model.PaymentOrder;
import com.orovia.payment.dto.PaymentOrderRequest;
import com.orovia.payment.dto.PaymentOrderResponse;
import java.time.OffsetDateTime;

/**
 * Mapper converting payment order objects.
 */
public class PaymentMapper {

    private PaymentMapper() {
    }

    /**
     * Maps a request to a new {@link PaymentOrder} skeleton.
     *
     * @param request incoming create request
     * @return new payment order instance
     */
    public static PaymentOrder toEntity(PaymentOrderRequest request, long id) {
        return PaymentOrder.builder()
                .id(id)
                .bookingId(request.getBookingId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentMethod(com.orovia.payment.domain.model.PaymentMethod.valueOf(request.getPaymentMethod()))
                .status(com.orovia.payment.domain.model.PaymentOrderStatus.CREATED)
                .returnUrl(request.getReturnUrl())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .type("PREPAID")
                .build();
    }

    /**
     * Converts a {@link PaymentOrder} to response.
     *
     * @param paymentOrder payment order
     * @param paymentUrl optional URL/token for client
     * @return response dto
     */
    public static PaymentOrderResponse toResponse(PaymentOrder paymentOrder, String paymentUrl) {
        return PaymentOrderResponse.builder()
                .id(paymentOrder.getId())
                .bookingId(paymentOrder.getBookingId())
                .amount(paymentOrder.getAmount())
                .currency(paymentOrder.getCurrency())
                .status(paymentOrder.getStatus().name())
                .paymentMethod(paymentOrder.getPaymentMethod().name())
                .externalOrderId(paymentOrder.getExternalPgOrderId())
                .paymentUrl(paymentUrl)
                .build();
    }
}
