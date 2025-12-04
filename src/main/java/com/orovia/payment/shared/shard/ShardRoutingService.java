package com.orovia.payment.shared.shard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Computes logical shard for a given routing key to enable multi-tenant scaling.
 */
@Component
public class ShardRoutingService {

    private static final Logger log = LoggerFactory.getLogger(ShardRoutingService.class);

    private final int shardCount;

    public ShardRoutingService(com.orovia.payment.shared.config.ScalingProperties scalingProperties) {
        this.shardCount = Math.max(1, scalingProperties.getShards().getTotal());
    }

    /**
     * Determine the shard identifier for a routing key.
     *
     * @param routingKey deterministic routing key
     * @return shard id string
     */
    public String resolveShard(String routingKey) {
        int hash = Math.abs(routingKey.hashCode());
        int shard = hash % shardCount;
        log.debug("Resolved shard {} for key {}", shard, routingKey);
        return "shard-" + shard;
    }
}
