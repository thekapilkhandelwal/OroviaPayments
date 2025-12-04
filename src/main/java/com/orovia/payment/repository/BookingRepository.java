package com.orovia.payment.repository;

import com.orovia.payment.domain.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for booking aggregates.
 */
public interface BookingRepository extends JpaRepository<Booking, Long> {
}
