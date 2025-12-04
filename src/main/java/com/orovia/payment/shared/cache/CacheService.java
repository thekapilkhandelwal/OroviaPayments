package com.orovia.payment.shared.cache;

import java.util.Optional;

/**
 * Abstraction for cache interactions to allow Redis or in-memory implementations.
 */
public interface CacheService {

    <T> void put(String key, T value, long ttlSeconds);

    <T> Optional<T> get(String key, Class<T> clazz);
}
