package com.orovia.payment.service;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@org.springframework.boot.autoconfigure.condition.ConditionalOnBean(StringRedisTemplate.class)
public class RedisIdempotencyService implements IdempotencyService {

    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean acquire(String key, Duration ttl) {
        Boolean stored = redisTemplate.opsForValue().setIfAbsent("idempo:" + key, "1", ttl);
        return Boolean.TRUE.equals(stored);
    }
}

