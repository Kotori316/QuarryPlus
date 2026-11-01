package com.yogpc.qp.fabric.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigSpec;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.config.ConfigDefiner;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;

/// [ConfigDefiner] for Night Config.
/// Call [#correct(Config)] after all values are defined to fix the invalid values and to put the comments.
public final class NightConfigDefiner implements ConfigDefiner {
    private final Config config;
    private final ConfigSpec spec = new ConfigSpec();
    private final Map<String, String> comments = new LinkedHashMap<>();
    private final List<String> sections = new ArrayList<>();

    public NightConfigDefiner(Config config) {
        this.config = config;
    }

    @Override
    public void push(String section) {
        sections.add(section);
    }

    @Override
    public void pop() {
        sections.removeLast();
    }

    @Override
    public BooleanSupplier defineBoolean(String key, boolean defaultValue, String comment) {
        var path = path(key);
        spec.define(path, defaultValue);
        comments.put(path, comment);
        return () -> config.getOrElse(path, defaultValue);
    }

    @Override
    public DoubleSupplier defineDouble(String key, double defaultValue, double min, double max, String comment) {
        var path = path(key);
        spec.defineInRange(path, defaultValue, min, max);
        comments.put(path, comment);
        return () -> config.<Number>getOrElse(path, defaultValue).doubleValue();
    }

    @Override
    public IntSupplier defineInt(String key, int defaultValue, int min, int max, String comment) {
        var path = path(key);
        spec.defineInRange(path, defaultValue, min, max);
        comments.put(path, comment);
        return () -> config.<Number>getOrElse(path, defaultValue).intValue();
    }

    public boolean isCorrect(Config target) {
        return spec.isCorrect(target);
    }

    public void correct(Config target) {
        spec.correct(target, (action, path, incorrectValue, correctedValue) ->
            QuarryPlus.LOGGER.debug("Config corrected '{}' ({}): {} -> {}", path, action, incorrectValue, correctedValue)
        );
        if (target instanceof CommentedConfig c) {
            comments.forEach(c::setComment);
        }
    }

    private String path(String key) {
        return sections.isEmpty() ? key : String.join(".", sections) + "." + key;
    }
}
