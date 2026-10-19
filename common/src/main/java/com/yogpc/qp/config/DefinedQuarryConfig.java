package com.yogpc.qp.config;

import com.mojang.serialization.JavaOps;
import com.yogpc.qp.machine.PowerMap;

import java.lang.reflect.RecordComponent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;

/// [QuarryConfig] whose values are defined via [ConfigDefiner].
/// Values are read from the suppliers every time, so the changes of the config file are reflected.
public final class DefinedQuarryConfig implements QuarryConfig {
    public DefinedQuarryConfig(ConfigDefiner definer, BooleanSupplier inDevelop) {
        this.debug = defineBoolean(definer, "debug", inDevelop.getAsBoolean(), "In debug mode");
        this.noEnergy = defineBoolean(definer, "noEnergy", false, "Enable No Energy mode");

        this.removeBedrockOnNetherTop = defineBoolean(definer, "removeBedrockOnNetherTop", inDevelop.getAsBoolean(), "Remove bedrock at y=127 in Nether");
        this.enableChunkLoader = defineBoolean(definer, "enableChunkLoader", true, "Enable Chunk Loader in machines");
        this.convertDeepslateOres = defineBoolean(definer, "convertDeepslateOres", false, "Convert Deepslate ores to normal ores");
        this.removeCommonMaterialsByChunkDestroyer = defineBoolean(definer, "removeCommonMaterialsByChunkDestroyer", true, "Remove common materials (Base blocks in Over world and the Nether) obtained by Chunk Destroyer");
        this.customPlayer = defineBoolean(definer, "customPlayer", false, "Enable Custom player mode");
        this.removeMinecartWithChest = defineBoolean(definer, "removeMinecartWithChest", true, "Remove Minecart with Chest when processing blocks");
        this.markerPlusRange = defineInt(definer, "markerPlusRange", 256, 8, 1 << 20, "Range of Marker Plus");
        this.flexibleMarkerRange = defineInt(definer, "flexibleMarkerRange", 256, 16, 1 << 24, "Range of Flexible Marker");
        this.digFromMaxY = defineBoolean(definer, "digFromMaxY", false, "Whether to start digging from the top of the frame. If false, it starts digging from the bottom of the frame");

        definer.push("powerMap");
        this.quarryPowerMap = defineInRecord(definer, "quarry", PowerMap.Default.QUARRY);
        this.advQuarryPowerMap = defineInRecord(definer, "advQuarry", PowerMap.Default.ADV_QUARRY);
        this.advPumpPowerMap = defineInRecord(definer, "advPump", PowerMap.Default.ADV_PUMP);
        definer.pop();

        this.enableMap = defineEnableMap(definer, "enableMap", inDevelop);
    }

    private final BooleanSupplier debug;
    private final BooleanSupplier noEnergy;
    private final BooleanSupplier removeBedrockOnNetherTop;
    private final BooleanSupplier enableChunkLoader;
    private final BooleanSupplier convertDeepslateOres;
    private final BooleanSupplier removeCommonMaterialsByChunkDestroyer;
    private final BooleanSupplier customPlayer;
    private final BooleanSupplier removeMinecartWithChest;
    private final IntSupplier markerPlusRange;
    private final IntSupplier flexibleMarkerRange;
    private final BooleanSupplier digFromMaxY;
    /// Key is the name of the record component in [PowerMap.Quarry].
    private final Map<String, DoubleSupplier> quarryPowerMap;
    /// Key is the name of the record component in [PowerMap.AdvQuarry].
    private final Map<String, DoubleSupplier> advQuarryPowerMap;
    /// Key is the name of the record component in [PowerMap.AdvPump].
    private final Map<String, DoubleSupplier> advPumpPowerMap;
    /// Key is the name of the machine.
    private final Map<String, BooleanSupplier> enableMap;

    @Override
    public boolean debug() {
        return debug.getAsBoolean();
    }

    @Override
    public boolean noEnergy() {
        return noEnergy.getAsBoolean();
    }

    @Override
    public PowerMap powerMap() {
        var map = Map.of(
                "quarry", quarryPowerMap,
                "advQuarry", advQuarryPowerMap,
                "advPump", advPumpPowerMap
            ).entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                section -> section.getValue().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getAsDouble()))
            ));
        return PowerMap.CODEC.codec().parse(JavaOps.INSTANCE, map).getOrThrow();
    }

    @Override
    public EnableMap enableMap() {
        return new EnableMap(enableMap.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getAsBoolean())));
    }

    @Override
    public double rebornEnergyConversionCoefficient() {
        // Fabric ONLY. Not configurable in other platforms.
        return 1d / 16d;
    }

    @Override
    public boolean removeBedrockOnNetherTop() {
        return removeBedrockOnNetherTop.getAsBoolean();
    }

    @Override
    public boolean enableChunkLoader() {
        return enableChunkLoader.getAsBoolean();
    }

    @Override
    public boolean convertDeepslateOres() {
        return convertDeepslateOres.getAsBoolean();
    }

    @Override
    public boolean removeCommonMaterialsByChunkDestroyer() {
        return removeCommonMaterialsByChunkDestroyer.getAsBoolean();
    }

    @Override
    public boolean customPlayer() {
        return customPlayer.getAsBoolean();
    }

    @Override
    public boolean removeMinecartWithChest() {
        return removeMinecartWithChest.getAsBoolean();
    }

    @Override
    public int markerPlusRange() {
        return markerPlusRange.getAsInt();
    }

    @Override
    public int flexibleMarkerRange() {
        return flexibleMarkerRange.getAsInt();
    }

    @Override
    public boolean digFromMaxY() {
        return digFromMaxY.getAsBoolean();
    }

    private static BooleanSupplier defineBoolean(ConfigDefiner definer, String key, boolean defaultValue, String comment) {
        return definer.defineBoolean(key, defaultValue, "%s. Default: %s".formatted(comment, defaultValue));
    }

    private static DoubleSupplier defineDouble(ConfigDefiner definer, String key, double defaultValue, double min, double max, String comment) {
        return definer.defineDouble(key, defaultValue, min, max, "%s. Default: %f, Min: %.1f, Max: %.1f".formatted(comment, defaultValue, min, max));
    }

    private static IntSupplier defineInt(ConfigDefiner definer, String key, int defaultValue, int min, int max, String comment) {
        return definer.defineInt(key, defaultValue, min, max, "%s. Default: %d, Min: %d, Max: %d".formatted(comment, defaultValue, min, max));
    }

    private static <T extends Record> Map<String, DoubleSupplier> defineInRecord(ConfigDefiner definer, String prefix, T instance) {
        var values = new LinkedHashMap<String, DoubleSupplier>();
        definer.push(prefix);
        try {
            for (RecordComponent field : instance.getClass().getRecordComponents()) {
                var defaultValue = (Double) field.getAccessor().invoke(instance);
                values.put(field.getName(), defineDouble(definer, field.getName(), defaultValue, 0d, 1e10, field.getName()));
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        } finally {
            definer.pop();
        }
        return values;
    }

    private static Map<String, BooleanSupplier> defineEnableMap(ConfigDefiner definer, String prefix, BooleanSupplier inDevelop) {
        var values = new LinkedHashMap<String, BooleanSupplier>();
        definer.push(prefix);
        try {
            EnableMap.getDefault(inDevelop).getMachinesMap().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    var name = e.getKey();
                    var defaultValue = e.getValue();
                    var comment = EnableMap.getDefaultValue(name) == EnableMap.EnableOrNot.ALWAYS_OFF
                        ? "This item can't be enabled in this platform. Configuration will be ignored. (%s)".formatted(name)
                        : "%s Default: %b".formatted(name, defaultValue);
                    values.put(name, definer.defineBoolean(name, defaultValue, comment));
                });
        } finally {
            definer.pop();
        }
        return values;
    }
}
