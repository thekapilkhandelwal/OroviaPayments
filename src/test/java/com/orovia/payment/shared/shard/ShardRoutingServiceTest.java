package com.orovia.payment.shared.shard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.orovia.payment.shared.config.ScalingProperties;
import org.junit.Before;
import org.junit.Test;

public class ShardRoutingServiceTest {

    private ShardRoutingService shardRoutingService;

    @Before
    public void setup() {
        ScalingProperties properties = new ScalingProperties();
        properties.getShards().setTotal(8);
        shardRoutingService = new ShardRoutingService(properties);
    }

    @Test
    public void resolvesConsistentShard() {
        String shard1 = shardRoutingService.resolveShard("booking-1");
        String shard2 = shardRoutingService.resolveShard("booking-1");
        assertEquals(shard1, shard2);
    }

    @Test
    public void separatesDifferentKeys() {
        String shard1 = shardRoutingService.resolveShard("booking-1");
        String shard2 = shardRoutingService.resolveShard("booking-2");
        assertNotEquals(shard1, shard2);
    }
}
