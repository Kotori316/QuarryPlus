package com.yogpc.qp.utils;

import java.util.function.Supplier;

final class ConstantEntry<T> extends CacheEntry<T> {
    private Supplier<T> supplier;

    ConstantEntry(Supplier<T> supplier) {
        super(Long.MAX_VALUE);
        this.supplier = supplier;
    }

    @Override
    protected void setNewCache() {
        if (cached == null) {
            cached = supplier.get();
            supplier = null; // GC
        }
    }
}
