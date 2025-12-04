package com.orovia.payment.repository;

import com.orovia.payment.domain.model.Settlement;
import com.orovia.payment.domain.model.SettlementStatus;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for settlements.
 */
public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    List<Settlement> findByHotelId(Long hotelId);

    List<Settlement> findByStatusAndCreatedAtBetween(SettlementStatus status, OffsetDateTime start, OffsetDateTime end);
}
