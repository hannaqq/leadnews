package com.news.model.persistence;

import java.time.Instant;
import java.util.function.LongSupplier;

final class SnowflakeSequence {

    static final long MAX_NODE_ID = 31;

    private static final long EPOCH = Instant.parse("2010-11-04T01:42:54.657Z").toEpochMilli();
    private static final long SEQUENCE_MASK = 4095;
    private static final int WORKER_SHIFT = 12;
    private static final int DATACENTER_SHIFT = 17;
    private static final int TIMESTAMP_SHIFT = 22;

    private final long workerId;
    private final long datacenterId;
    private final LongSupplier clock;

    private long sequence;
    private long lastTimestamp = -1;

    SnowflakeSequence(long workerId, long datacenterId) {
        this(workerId, datacenterId, System::currentTimeMillis);
    }

    SnowflakeSequence(long workerId, long datacenterId, LongSupplier clock) {
        this.workerId = validateNodeId("worker-id", workerId);
        this.datacenterId = validateNodeId("datacenter-id", datacenterId);
        this.clock = clock;
    }

    synchronized long nextId() {
        long timestamp = clock.getAsLong();
        if (timestamp < EPOCH) {
            throw new IllegalStateException("System clock is before the Snowflake epoch");
        }
        if (timestamp < lastTimestamp) {
            throw new IllegalStateException(
                    "System clock moved backwards by " + (lastTimestamp - timestamp) + " ms");
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                timestamp = waitForNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0;
        }

        lastTimestamp = timestamp;
        return ((timestamp - EPOCH) << TIMESTAMP_SHIFT)
                | (datacenterId << DATACENTER_SHIFT)
                | (workerId << WORKER_SHIFT)
                | sequence;
    }

    private long waitForNextMillis(long timestamp) {
        long current = clock.getAsLong();
        while (current <= timestamp) {
            Thread.onSpinWait();
            current = clock.getAsLong();
        }
        return current;
    }

    private static long validateNodeId(String name, long value) {
        if (value < 0 || value > MAX_NODE_ID) {
            throw new IllegalArgumentException(name + " must be between 0 and " + MAX_NODE_ID);
        }
        return value;
    }
}
