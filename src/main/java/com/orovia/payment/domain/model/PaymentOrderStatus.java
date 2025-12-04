package com.orovia.payment.domain.model;

/**
 * Lifecycle states for a payment order.
 */
public enum PaymentOrderStatus {
    CREATED,
    PENDING,
    SUCCESS,
    FAILED,
    REFUNDED
}
