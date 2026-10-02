package com.news.model.persistence;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnowflakeSequenceTest {

    private static final long TEST_TIME = 1_800_000_000_000L;

    @Test
    void rejectsNodeIdsOutsideFiveBitRange() {
        assertThrows(IllegalArgumentException.class, () -> new SnowflakeSequence(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> new SnowflakeSequence(32, 0));
        assertThrows(IllegalArgumentException.class, () -> new SnowflakeSequence(0, -1));
        assertThrows(IllegalArgumentException.class, () -> new SnowflakeSequence(0, 32));
    }

    @Test
    void generatesUniqueIdsAcrossConcurrentCalls() throws Exception {
        SnowflakeSequence sequence = new SnowflakeSequence(1, 1);
        int threads = 8;
        int idsPerThread = 2_000;
        Set<Long> ids = ConcurrentHashMap.newKeySet();
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        try {
            for (int i = 0; i < threads; i++) {
                executor.submit(() -> {
                    ready.countDown();
                    try {
                        start.await();
                        for (int j = 0; j < idsPerThread; j++) {
                            ids.add(sequence.nextId());
                        }
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                    }
                });
            }

            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();
            executor.shutdown();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
            assertEquals(threads * idsPerThread, ids.size());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void nodeCoordinatesProduceDifferentIdsAtTheSameTimestamp() {
        SnowflakeSequence first = new SnowflakeSequence(1, 1, () -> TEST_TIME);
        SnowflakeSequence second = new SnowflakeSequence(2, 1, () -> TEST_TIME);
        SnowflakeSequence third = new SnowflakeSequence(1, 2, () -> TEST_TIME);

        assertNotEquals(first.nextId(), second.nextId());
        assertNotEquals(first.nextId(), third.nextId());
    }

    @Test
    void rejectsClockRollback() {
        AtomicLong clock = new AtomicLong(TEST_TIME);
        SnowflakeSequence sequence = new SnowflakeSequence(0, 0, clock::get);
        sequence.nextId();
        clock.decrementAndGet();

        IllegalStateException exception = assertThrows(IllegalStateException.class, sequence::nextId);
        assertTrue(exception.getMessage().contains("moved backwards"));
    }

    @Test
    void waitsForNextMillisecondWhenSequenceIsExhausted() {
        AtomicInteger reads = new AtomicInteger();
        SnowflakeSequence sequence = new SnowflakeSequence(
                0,
                0,
                () -> reads.incrementAndGet() <= 4097 ? TEST_TIME : TEST_TIME + 1);
        Set<Long> ids = ConcurrentHashMap.newKeySet();

        for (int i = 0; i < 4097; i++) {
            ids.add(sequence.nextId());
        }

        assertEquals(4097, ids.size());
    }
}
