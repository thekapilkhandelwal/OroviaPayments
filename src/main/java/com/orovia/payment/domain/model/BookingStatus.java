package com.orovia.payment.domain.model;

/**
 * Status for hotel bookings managed by Orovia.
 */
public enum BookingStatus {
    PENDING_PAYMENT,
    CONFIRMED,
    CONFIRMED_PENDING_PAYMENT,
    CANCELLED
}
