package com.yogpc.qp.config;

import com.yogpc.qp.PlatformAccess;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

public final class EnableMap {
    private final Map<String, Boolean> machinesMap;

    public EnableMap(Map<String, Boolean> machinesMap) {
        this.machinesMap = machinesMap;
    }

    public EnableMap() {
        this(new HashMap<>());
    }

    public boolean enabled(String name) {
        var defaultSetting = getDefaultValue(name);
        if (defaultSetting == EnableOrNot.ALWAYS_ON) {
            return true;
        }
        if (defaultSetting == EnableOrNot.ALWAYS_OFF) {
            return false;
        }
        var value = machinesMap.get(name);
        if (value == null) {
            return false;
        }
        return value;
    }

    public void set(String name, boolean value) {
        machinesMap.put(name, value);
    }

    Map<String, Boolean> getMachinesMap() {
        return machinesMap;
    }

    static EnableMap getDefault(BooleanSupplier inDevelop) {
        if (PlatformAccess.getAccess().registerObjects() == null) {
            // In unit test
            return new EnableMap();
        }
        var map = PlatformAccess.getAccess().registerObjects().defaultEnableSetting().entrySet().stream()
            .filter(e -> e.getValue().configurable())
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().on() || inDevelop.getAsBoolean()));
        return new EnableMap(map);
    }

    static EnableMap from(Map<String, Object> config) {
        return new EnableMap(config.entrySet().stream()
            .map(e -> Map.entry(e.getKey(), (Boolean) e.getValue()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    static EnableOrNot getDefaultValue(String name) {
        if (PlatformAccess.getAccess().registerObjects() != null) return null;

        return PlatformAccess.getAccess().registerObjects().defaultEnableSetting().get(name);
    }

    public enum EnableOrNot {
        CONFIG_ON, CONFIG_OFF, ALWAYS_ON, ALWAYS_OFF;

        public boolean configurable() {
            return this == CONFIG_ON || this == CONFIG_OFF || this == ALWAYS_OFF;
        }

        public boolean on() {
            return this == CONFIG_ON || this == ALWAYS_ON;
        }
    }
}
