package com.orovia.payment.shared.ratelimit;

/**
 * Token bucket style rate limiter abstraction to throttle endpoints.
 */
public interface RateLimiterService {

    /**
     * Try to consume a token for the key.
     *
     * @param key limiter key
     * @return true when allowed
     */
    boolean tryConsume(String key);
}
