package com.yogpc.qp.config;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;

/// Adapter of platform-specific config builders, such as `ModConfigSpec.Builder` and `ForgeConfigSpec.Builder`.
///
/// Each method defines a value in the current section and returns a supplier of the current value.
public interface ConfigDefiner {
    void push(String section);

    void pop();

    BooleanSupplier defineBoolean(String key, boolean defaultValue, String comment);

    DoubleSupplier defineDouble(String key, double defaultValue, double min, double max, String comment);

    IntSupplier defineInt(String key, int defaultValue, int min, int max, String comment);
}
