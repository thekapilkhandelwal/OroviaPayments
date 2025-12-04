package com.orovia.payment.shared.id;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

/**
 * Snowflake-like generator with time and datacenter hints suitable for multi-region deployments.
 */
@Component
public class SnowflakeIdGenerator implements IdGenerator {

    private static final long EPOCH = 1700000000000L;
    private static final int DATACENTER_BITS = 5;
    private static final int WORKER_BITS = 5;
    private static final int SEQUENCE_BITS = 12;

    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);
    private static final long WORKER_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_SHIFT = SEQUENCE_BITS + WORKER_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_BITS + DATACENTER_BITS;

    private final long dataCenterId;
    private final long workerId;
    private final AtomicLong sequence = new AtomicLong(0);
    private volatile long lastTimestamp = -1L;

    public SnowflakeIdGenerator() {
        this(1, 1);
    }

    public SnowflakeIdGenerator(long dataCenterId, long workerId) {
        this.dataCenterId = dataCenterId;
        this.workerId = workerId;
    }

    @Override
    public synchronized long nextId() {
        long timestamp = currentTime();
        if (timestamp < lastTimestamp) {
            timestamp = lastTimestamp;
        }
        if (timestamp == lastTimestamp) {
            long next = (sequence.incrementAndGet()) & SEQUENCE_MASK;
            if (next == 0) {
                timestamp = waitNextMillis(lastTimestamp);
            }
            sequence.set(next);
        } else {
            sequence.set(0);
        }
        lastTimestamp = timestamp;
        return ((timestamp - EPOCH) << TIMESTAMP_SHIFT)
                | (dataCenterId << DATACENTER_SHIFT)
                | (workerId << WORKER_SHIFT)
                | sequence.get();
    }

    private long waitNextMillis(long last) {
        long ts = currentTime();
        while (ts <= last) {
            ts = currentTime();
        }
        return ts;
    }

    private long currentTime() {
        return Instant.now().toEpochMilli();
    }
}
