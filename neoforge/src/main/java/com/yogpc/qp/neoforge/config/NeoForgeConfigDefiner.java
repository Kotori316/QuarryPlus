package com.yogpc.qp.neoforge.config;

import com.yogpc.qp.config.ConfigDefiner;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;

/// Defines config values with [ModConfigSpec.Builder].
public final class NeoForgeConfigDefiner implements ConfigDefiner {
    private final ModConfigSpec.Builder builder;

    public NeoForgeConfigDefiner(ModConfigSpec.Builder builder) {
        this.builder = builder;
    }

    @Override
    public void push(String section) {
        builder.push(section);
    }

    @Override
    public void pop() {
        builder.pop();
    }

    @Override
    public BooleanSupplier defineBoolean(String key, boolean defaultValue, String comment) {
        return builder.comment(comment).define(key, defaultValue);
    }

    @Override
    public DoubleSupplier defineDouble(String key, double defaultValue, double min, double max, String comment) {
        return builder.comment(comment).defineInRange(key, defaultValue, min, max);
    }

    @Override
    public IntSupplier defineInt(String key, int defaultValue, int min, int max, String comment) {
        return builder.comment(comment).defineInRange(key, defaultValue, min, max);
    }
}
