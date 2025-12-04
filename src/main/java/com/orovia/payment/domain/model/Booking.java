package com.orovia.payment.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Booking aggregate represents a hotel booking on the platform.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long hotelId;
    private Long customerId;
    private BigDecimal amount;
    private String currency;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
