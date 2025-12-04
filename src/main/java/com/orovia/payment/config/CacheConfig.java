package com.orovia.payment.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cache configuration; can be swapped with Redis via profile.
 */
@Configuration
public class CacheConfig {

    /**
     * Default in-memory cache manager for dev/test.
     *
     * @return cache manager
     */
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("paymentStatus", "bookings");
    }
}
