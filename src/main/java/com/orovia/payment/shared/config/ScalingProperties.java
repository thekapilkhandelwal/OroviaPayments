package com.orovia.payment.shared.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Centralized scaling knobs for rate limits, shards, caches and eventing.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "payment")
public class ScalingProperties {

    private RateLimit rateLimit = new RateLimit();
    private Cache cache = new Cache();
    private Shards shards = new Shards();

    @Data
    public static class RateLimit {
        private long createOrderPermitPerSecond = 200;
        private long webhookPermitPerSecond = 500;
    }

    @Data
    public static class Cache {
        private long statusTtlSeconds = 60;
    }

    @Data
    public static class Shards {
        private int total = 8;
        private String regionHint = "us-east";
    }
}
