package com.orovia.payment.repository;

import com.orovia.payment.domain.model.HotelAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for hotel payable balances.
 */
public interface HotelAccountRepository extends JpaRepository<HotelAccount, Long> {
    Optional<HotelAccount> findByHotelId(Long hotelId);
}
