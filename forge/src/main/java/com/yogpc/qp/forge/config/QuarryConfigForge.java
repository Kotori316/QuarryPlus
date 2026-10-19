package com.yogpc.qp.forge.config;

import com.mojang.serialization.JavaOps;
import com.yogpc.qp.config.EnableMap;
import com.yogpc.qp.config.QuarryConfig;
import com.yogpc.qp.machine.PowerMap;
import net.minecraftforge.common.ForgeConfigSpec;

import java.lang.reflect.RecordComponent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

public final class QuarryConfigForge implements QuarryConfig {
    public QuarryConfigForge(ForgeConfigSpec.Builder builder, BooleanSupplier inDevelop) {
        this.debug = defineBoolean(builder, "debug", inDevelop.getAsBoolean(), "In debug mode");
        this.noEnergy = defineBoolean(builder, "noEnergy", false, "Enable No Energy mode");

        this.removeBedrockOnNetherTop = defineBoolean(builder, "removeBedrockOnNetherTop", inDevelop.getAsBoolean(), "Remove bedrock at y=127 in Nether");
        this.enableChunkLoader = defineBoolean(builder, "enableChunkLoader", true, "Enable Chunk Loader in machines");
        this.convertDeepslateOres = defineBoolean(builder, "convertDeepslateOres", false, "Convert Deepslate ores to normal ores");
        this.removeCommonMaterialsByChunkDestroyer = defineBoolean(builder, "removeCommonMaterialsByChunkDestroyer", true, "Remove common materials(Base blocks in Over world and the Nether) obtained by Chunk Destroyer");
        this.customPlayer = defineBoolean(builder, "customPlayer", false, "Enable Custom player mode");
        this.removeMinecartWithChest = defineBoolean(builder, "removeMinecartWithChest", true, "Remove Minecart with Chest when processing blocks");
        this.markerPlusRange = defineInt(builder, "markerPlusRange", 256, 8, 1 << 20, "Range of Marker Plus");
        this.flexibleMarkerRange = defineInt(builder, "flexibleMarkerRange", 256, 16, 1 << 24, "Range of Flexible Marker");
        this.digFromMaxY = defineBoolean(builder, "digFromMaxY", false, "Whether to start digging from the top of the frame. If false, it starts digging from the bottom of the frame.");

        builder.push("powerMap");
        this.quarryPowerMap = defineInRecord(builder, "quarry", PowerMap.Default.QUARRY);
        this.advQuarryPowerMap = defineInRecord(builder, "advQuarry", PowerMap.Default.ADV_QUARRY);
        this.advPumpPowerMap = defineInRecord(builder, "advPump", PowerMap.Default.ADV_PUMP);
        builder.pop();

        this.enableMap = defineEnableMap(builder, "enableMap", inDevelop);
    }

    private final ForgeConfigSpec.BooleanValue debug;
    private final ForgeConfigSpec.BooleanValue noEnergy;
    private final ForgeConfigSpec.BooleanValue removeBedrockOnNetherTop;
    private final ForgeConfigSpec.BooleanValue enableChunkLoader;
    private final ForgeConfigSpec.BooleanValue convertDeepslateOres;
    private final ForgeConfigSpec.BooleanValue removeCommonMaterialsByChunkDestroyer;
    private final ForgeConfigSpec.BooleanValue customPlayer;
    private final ForgeConfigSpec.BooleanValue removeMinecartWithChest;
    private final ForgeConfigSpec.IntValue markerPlusRange;
    private final ForgeConfigSpec.IntValue flexibleMarkerRange;
    private final ForgeConfigSpec.BooleanValue digFromMaxY;
    /// Key is the name of the record component in [PowerMap.Quarry].
    private final Map<String, ForgeConfigSpec.DoubleValue> quarryPowerMap;
    /// Key is the name of the record component in [PowerMap.AdvQuarry].
    private final Map<String, ForgeConfigSpec.DoubleValue> advQuarryPowerMap;
    /// Key is the name of the record component in [PowerMap.AdvPump].
    private final Map<String, ForgeConfigSpec.DoubleValue> advPumpPowerMap;
    /// Key is the name of the machine.
    private final Map<String, ForgeConfigSpec.BooleanValue> enableMap;

    @Override
    public boolean debug() {
        return debug.get();
    }

    @Override
    public boolean noEnergy() {
        return noEnergy.get();
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
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()))
            ));
        return PowerMap.CODEC.codec().parse(JavaOps.INSTANCE, map).getOrThrow();
    }

    @Override
    public EnableMap enableMap() {
        return new EnableMap(enableMap.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get())));
    }

    @Override
    public double rebornEnergyConversionCoefficient() {
        // Fabric ONLY. Not configurable in Forge.
        return 1d / 16d;
    }

    @Override
    public boolean removeBedrockOnNetherTop() {
        return removeBedrockOnNetherTop.get();
    }

    @Override
    public boolean enableChunkLoader() {
        return enableChunkLoader.get();
    }

    @Override
    public boolean convertDeepslateOres() {
        return convertDeepslateOres.get();
    }

    @Override
    public boolean removeCommonMaterialsByChunkDestroyer() {
        return removeCommonMaterialsByChunkDestroyer.get();
    }

    @Override
    public boolean customPlayer() {
        return customPlayer.get();
    }

    @Override
    public boolean removeMinecartWithChest() {
        return removeMinecartWithChest.get();
    }

    @Override
    public int markerPlusRange() {
        return markerPlusRange.get();
    }

    @Override
    public int flexibleMarkerRange() {
        return flexibleMarkerRange.get();
    }

    @Override
    public boolean digFromMaxY() {
        return digFromMaxY.get();
    }

    private static ForgeConfigSpec.BooleanValue defineBoolean(ForgeConfigSpec.Builder builder, String key, boolean defaultValue, String comment) {
        return builder
            .comment("%s. Default: %s".formatted(comment, defaultValue))
            .define(key, defaultValue);
    }

    private static ForgeConfigSpec.DoubleValue defineDouble(ForgeConfigSpec.Builder builder, String key, double defaultValue, double min, double max, String comment) {
        return builder
            .comment("%s. Default: %f, Min: %.1f, Max: %.1f".formatted(comment, defaultValue, min, max))
            .defineInRange(key, defaultValue, min, max);
    }

    private static ForgeConfigSpec.IntValue defineInt(ForgeConfigSpec.Builder builder, String key, int defaultValue, int min, int max, String comment) {
        return builder
            .comment("%s. Default: %d, Min: %d, Max: %d".formatted(comment, defaultValue, min, max))
            .defineInRange(key, defaultValue, min, max);
    }

    private static <T extends Record> Map<String, ForgeConfigSpec.DoubleValue> defineInRecord(ForgeConfigSpec.Builder builder, String prefix, T instance) {
        var values = new LinkedHashMap<String, ForgeConfigSpec.DoubleValue>();
        builder.push(prefix);
        try {
            for (RecordComponent field : instance.getClass().getRecordComponents()) {
                var defaultValue = (Double) field.getAccessor().invoke(instance);
                values.put(field.getName(), defineDouble(builder, field.getName(), defaultValue, 0d, 1e10, field.getName()));
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        } finally {
            builder.pop();
        }
        return values;
    }

    private static Map<String, ForgeConfigSpec.BooleanValue> defineEnableMap(ForgeConfigSpec.Builder builder, String prefix, BooleanSupplier inDevelop) {
        var values = new LinkedHashMap<String, ForgeConfigSpec.BooleanValue>();
        builder.push(prefix);
        try {
            EnableMap.getDefault(inDevelop).getMachinesMap().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    var name = e.getKey();
                    var defaultValue = e.getValue();
                    if (EnableMap.getDefaultValue(name) == EnableMap.EnableOrNot.ALWAYS_OFF) {
                        builder.comment("This item can't be enabled in this platform. Configuration will be ignored. (%s)".formatted(name));
                    } else {
                        builder.comment("%s Default: %b".formatted(name, defaultValue));
                    }
                    values.put(name, builder.define(name, (boolean) defaultValue));
                });
        } finally {
            builder.pop();
        }
        return values;
    }
}
