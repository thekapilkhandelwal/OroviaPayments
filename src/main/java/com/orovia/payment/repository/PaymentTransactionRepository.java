package com.orovia.payment.repository;

import com.orovia.payment.domain.model.PaymentTransaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for payment transaction attempts.
 */
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    List<PaymentTransaction> findByPaymentOrderId(Long paymentOrderId);
}
