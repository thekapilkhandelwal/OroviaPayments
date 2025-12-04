package com.orovia.payment.shared.ratelimit;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

/**
 * Lightweight local token bucket used for throttling hot endpoints.
 */
@Component
public class InMemoryTokenBucketRateLimiter implements RateLimiterService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final long defaultRefillPerSecond;

    public InMemoryTokenBucketRateLimiter(com.orovia.payment.shared.config.ScalingProperties scalingProperties) {
        this.defaultRefillPerSecond = scalingProperties.getRateLimit().getCreateOrderPermitPerSecond();
    }

    @Override
    public boolean tryConsume(String key) {
        Bucket bucket = buckets.computeIfAbsent(key, ignored -> new Bucket(defaultRefillPerSecond));
        return bucket.tryConsume();
    }

    private static class Bucket {

        private final long refillPerSecond;
        private final long capacity;
        private final AtomicLong tokens;
        private volatile long lastRefillTimestamp;

        Bucket(long refillPerSecond) {
            this.refillPerSecond = Math.max(1, refillPerSecond);
            this.capacity = this.refillPerSecond * 2;
            this.tokens = new AtomicLong(this.capacity);
            this.lastRefillTimestamp = Instant.now().getEpochSecond();
        }

        boolean tryConsume() {
            refill();
            long current = tokens.get();
            if (current <= 0) {
                return false;
            }
            return tokens.compareAndSet(current, current - 1);
        }

        private void refill() {
            long now = Instant.now().getEpochSecond();
            long elapsed = now - lastRefillTimestamp;
            if (elapsed <= 0) {
                return;
            }
            long refillAmount = Math.min(capacity, tokens.get() + elapsed * refillPerSecond);
            tokens.set(refillAmount);
            lastRefillTimestamp = now;
        }
    }
}
