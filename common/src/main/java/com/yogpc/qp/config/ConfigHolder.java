package com.yogpc.qp.config;

import java.util.function.Supplier;

public final class ConfigHolder implements Supplier<QuarryConfig> {
    private final Supplier<? extends QuarryConfig> supplier;
    private QuarryConfig instance = null;

    public ConfigHolder(Supplier<? extends QuarryConfig> supplier) {
        this.supplier = supplier;
    }

    @Override
    public QuarryConfig get() {
        if (instance == null) {
            instance = supplier.get();
        }
        return instance;
    }

    public void reset() {
        instance = null;
    }
}
