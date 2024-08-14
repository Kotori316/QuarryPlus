package com.yogpc.qp.config;

import com.yogpc.qp.machine.marker.NormalMarkerBlock;
import com.yogpc.qp.machine.misc.GeneratorBlock;
import net.minecraft.util.GsonHelper;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class EnableMap {
    public static final Set<String> ALWAYS_ON = Set.of(
        NormalMarkerBlock.NAME,
        GeneratorBlock.NAME
    );
    private final Map<String, Boolean> machinesMap;

    public EnableMap(Map<String, Boolean> machinesMap) {
        this.machinesMap = machinesMap;
    }

    public EnableMap() {
        this(new HashMap<>());
    }

    public boolean enabled(String name) {
        if (ALWAYS_ON.contains(name)) {
            return true;
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

    static EnableMap getDefault() {
        var defaultConfig = GsonHelper.parse(new InputStreamReader(
            Objects.requireNonNull(EnableMap.class.getResourceAsStream("/machine_default.json"), "Content in Jar must not be absent.")
        ));
        var map = defaultConfig.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getAsBoolean()));
        return new EnableMap(map);
    }

    static EnableMap from(Map<String, Object> config) {
        return new EnableMap(config.entrySet().stream()
            .map(e -> Map.entry(e.getKey(), (Boolean) e.getValue()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }
}
