package com.yogpc.qp.utils;

import net.minecraft.world.level.Level;

import java.util.function.Supplier;

/**
 * Cache for data.
 * Default implementation is not for multi thread.
 */
public abstract class CacheEntry<T> {
    protected long lastUpdateTime = 0L;
    protected final long cacheInterval;
    protected T cached;

    protected CacheEntry(long cacheInterval) {
        this.cacheInterval = cacheInterval;
    }

    public T getValue(long currentTime) {
        if (currentTime - lastUpdateTime >= cacheInterval || cached == null) {
            // Update
            this.setNewCache();
            lastUpdateTime = currentTime;
        }
        return cached;
    }

    public T getValue(Level level) {
        if (level == null) {
            return getValue(0);
        } else {
            return getValue(level.getGameTime());
        }
    }

    protected abstract void setNewCache();

    /**
     * Force this cache to get new value next time.
     */
    public void expire() {
        cached = null;
    }

    @Override
    public String toString() {
        return "CacheEntry{" +
            "cached=" + cached +
            '}';
    }

    public static <T> CacheEntry<T> supplierCache(long cacheInterval, Supplier<T> supplier) {
        return new SupplierCacheEntry<>(cacheInterval, supplier);
    }
}
