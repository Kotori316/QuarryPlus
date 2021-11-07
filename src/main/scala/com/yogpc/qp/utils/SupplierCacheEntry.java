package com.yogpc.qp.utils;

import java.util.function.Supplier;

class SupplierCacheEntry<T> extends CacheEntry<T> {
    private final Supplier<T> supplier;

    public SupplierCacheEntry(long cacheInterval, Supplier<T> supplier) {
        super(cacheInterval);
        this.supplier = supplier;
    }

    @Override
    protected void setNewCache() {
        this.cached = this.supplier.get();
    }
}
