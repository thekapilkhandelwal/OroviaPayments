package com.orovia.payment.repository;

import com.orovia.payment.domain.model.Refund;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for refunds.
 */
public interface RefundRepository extends JpaRepository<Refund, Long> {
    List<Refund> findByPaymentOrderId(Long paymentOrderId);
}
