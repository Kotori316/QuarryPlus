package com.yogpc.qp.forge.config;

import com.yogpc.qp.config.ConfigDefiner;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;

/// Defines config values with [ForgeConfigSpec.Builder].
public final class ForgeConfigDefiner implements ConfigDefiner {
    private final ForgeConfigSpec.Builder builder;

    public ForgeConfigDefiner(ForgeConfigSpec.Builder builder) {
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
        var value = builder.comment(comment).define(key, defaultValue);
        return value::get;
    }

    @Override
    public DoubleSupplier defineDouble(String key, double defaultValue, double min, double max, String comment) {
        var value = builder.comment(comment).defineInRange(key, defaultValue, min, max);
        return value::get;
    }

    @Override
    public IntSupplier defineInt(String key, int defaultValue, int min, int max, String comment) {
        var value = builder.comment(comment).defineInRange(key, defaultValue, min, max);
        return value::get;
    }
}
