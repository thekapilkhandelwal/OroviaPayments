package com.orovia.payment.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class IdempotencyServiceTest {

    @Test
    public void shouldAllowOnlyFirstAcquisitionForSameKey() throws Exception {
        InMemoryIdempotencyService service = new InMemoryIdempotencyService();
        int calls = 10;
        CountDownLatch latch = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(calls);
        List<Future<Boolean>> futures = new ArrayList<>();
        for (int i = 0; i < calls; i++) {
            futures.add(executor.submit(() -> {
                latch.await();
                return service.acquire("k", Duration.ofSeconds(5));
            }));
        }
        latch.countDown();

        long successCount = futures.stream().filter(f -> {
            try {
                return f.get();
            } catch (Exception e) {
                return false;
            }
        }).count();
        assertEquals(1L, successCount);
        executor.shutdownNow();
    }
}

