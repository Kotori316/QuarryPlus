package com.yogpc.qp.utils;

import java.util.concurrent.atomic.AtomicInteger;

import com.yogpc.qp.QuarryPlusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CacheEntryTest extends QuarryPlusTest {
    @Test
    void instance() {
        var cache = CacheEntry.supplierCache(1, () -> 4);
        assertEquals(4, cache.getValue(0));
    }

    @Test
    void initial() {
        var cache = CacheEntry.supplierCache(1, () -> "Initial");
        assertEquals(0, cache.lastUpdateTime);
        assertNull(cache.cached);
    }

    @Test
    void updateFirst() {
        var reference = new AtomicInteger(15);
        var cache = CacheEntry.supplierCache(1, () -> reference.getAndSet(4));

        assertEquals(15, reference.get());
        assertEquals(15, cache.getValue(1));
        assertEquals(1, cache.lastUpdateTime);
        assertEquals(4, reference.get());
    }

    @Test
    void updateNext1() {
        var reference = new AtomicInteger(15);
        var cache = CacheEntry.supplierCache(1, () -> reference.getAndSet(4));

        cache.getValue(1);
        assertEquals(15, cache.getValue(1));
        assertEquals(4, cache.getValue(2));
        assertEquals(4, reference.get());
        assertEquals(2, cache.lastUpdateTime);
    }

    @Test
    void updateNext2() {
        var reference = new AtomicInteger(8);
        var cache = CacheEntry.supplierCache(1, reference::getAndIncrement);

        assertEquals(8, reference.get());
        assertEquals(8, cache.getValue(1));
        assertEquals(9, reference.get());
        assertEquals(8, cache.getValue(1));
        assertEquals(1, cache.lastUpdateTime);

        assertEquals(9, cache.getValue(2));
        assertEquals(10, reference.get());
        assertEquals(2, cache.lastUpdateTime);


        assertEquals(10, cache.getValue(15));
        assertEquals(11, reference.get());
        assertEquals(15, cache.lastUpdateTime);
    }

    @Test
    void interval2() {
        var reference = new AtomicInteger(8);
        var cache = CacheEntry.supplierCache(2, reference::getAndIncrement);
        assertEquals(8, cache.getValue(0));
        assertEquals(9, reference.get());
        assertEquals(8, cache.getValue(1));
        assertEquals(9, reference.get(), "Shouldn't be updated");
        assertEquals(9, cache.getValue(2));
        assertEquals(10, reference.get());
        assertEquals(10, cache.getValue(4));
        assertEquals(11, reference.get());
    }

    @Test
    void expire() {
        var reference = new AtomicInteger(8);
        var cache = CacheEntry.supplierCache(10, reference::getAndIncrement);
        cache.getValue(0);
        assertEquals(9, reference.get());
        cache.expire();
        assertEquals(9, reference.get(), "Shouldn't be updated when expire is called.");
        assertEquals(9, cache.getValue(5));
        assertEquals(10, reference.get());
        assertEquals(9, cache.getValue(14));
        assertEquals(10, reference.get());
        assertEquals(10, cache.getValue(15));
        assertEquals(11, reference.get());
    }
}
