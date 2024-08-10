package com.yogpc.qp.config;

import net.minecraft.util.GsonHelper;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

public final class EnableMap {
    private final Map<String, BooleanSupplier> machinesMap;

    public EnableMap(Map<String, BooleanSupplier> machinesMap) {
        this.machinesMap = machinesMap;
    }

    public EnableMap() {
        this(new HashMap<>());
    }

    public boolean enabled(String name) {
        var supplier = machinesMap.get(name);
        if (supplier == null) {
            return false;
        }
        return supplier.getAsBoolean();
    }

    public void set(String name, boolean value) {
        machinesMap.put(name, () -> value);
    }

    Map<String, BooleanSupplier> getMachinesMap() {
        return machinesMap;
    }

    static EnableMap getDefault() {
        var defaultConfig = GsonHelper.parse(new InputStreamReader(
            Objects.requireNonNull(EnableMap.class.getResourceAsStream("/machine_default.json"), "Content in Jar must not be absent.")
        ));
        var map = defaultConfig.entrySet().stream()
            .map(e -> Map.entry(e.getKey(), e.getValue().getAsBoolean()))
            .map(e -> Map.<String, BooleanSupplier>entry(e.getKey(), e::getValue))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new EnableMap(map);
    }

    static EnableMap from(Map<String, Object> config) {
        return new EnableMap(config.entrySet().stream()
            .map(e -> Map.<String, BooleanSupplier>entry(e.getKey(), () -> (Boolean) e.getValue()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }
}
