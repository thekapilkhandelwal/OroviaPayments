package com.orovia.payment.repository;

import com.orovia.payment.domain.model.PaymentOrder;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for payment orders.
 */
public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {

    Optional<PaymentOrder> findByBookingIdAndPaymentMethod(Long bookingId, com.orovia.payment.domain.model.PaymentMethod paymentMethod);

    Optional<PaymentOrder> findByIdempotencyKey(String idempotencyKey);

    Optional<PaymentOrder> findByExternalPgOrderId(String externalPgOrderId);
}
