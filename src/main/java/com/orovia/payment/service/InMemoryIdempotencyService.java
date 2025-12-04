package com.orovia.payment.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnMissingBean(RedisIdempotencyService.class)
public class InMemoryIdempotencyService implements IdempotencyService {

    private final Map<String, Instant> expirations = new ConcurrentHashMap<>();

    @Override
    public boolean acquire(String key, Duration ttl) {
        Instant now = Instant.now();
        expirations.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
        Instant existing = expirations.putIfAbsent(key, now.plus(ttl));
        return existing == null || existing.isBefore(now);
    }
}

