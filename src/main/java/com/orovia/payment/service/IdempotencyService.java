package com.orovia.payment.service;

import java.time.Duration;

/**
 * Shared idempotency guard for external callbacks and client supplied keys.
 */
public interface IdempotencyService {

    /**
     * Attempts to reserve a key for a duration.
     *
     * @param key unique idempotency key
     * @param ttl time to live
     * @return true if the caller acquired the key and can proceed
     */
    boolean acquire(String key, Duration ttl);
}

