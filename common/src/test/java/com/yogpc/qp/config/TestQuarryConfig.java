package com.yogpc.qp.config;

import com.yogpc.qp.machine.PowerMap;

import java.util.HashMap;

/// In-memory [QuarryConfig] for tests. Use [#defaults()] and `with*` methods to create an instance.
public record TestQuarryConfig(
    boolean debug,
    boolean noEnergy,
    PowerMap powerMap,
    EnableMap enableMap,
    double rebornEnergyConversionCoefficient,
    boolean removeBedrockOnNetherTop,
    boolean enableChunkLoader,
    boolean convertDeepslateOres,
    boolean removeCommonMaterialsByChunkDestroyer,
    boolean customPlayer,
    boolean removeMinecartWithChest,
    int markerPlusRange,
    int flexibleMarkerRange,
    boolean digFromMaxY
) implements QuarryConfig {
    public static TestQuarryConfig defaults() {
        return new TestQuarryConfig(
            false,
            false,
            new PowerMap(PowerMap.Default.QUARRY, PowerMap.Default.ADV_QUARRY, PowerMap.Default.ADV_PUMP),
            new EnableMap(new HashMap<>()),
            1d / 16d,
            false,
            true,
            false,
            true,
            false,
            true,
            256,
            256,
            false
        );
    }

    public TestQuarryConfig withDebug(boolean debug) {
        return new TestQuarryConfig(debug, noEnergy, powerMap, enableMap, rebornEnergyConversionCoefficient, removeBedrockOnNetherTop, enableChunkLoader, convertDeepslateOres, removeCommonMaterialsByChunkDestroyer, customPlayer, removeMinecartWithChest, markerPlusRange, flexibleMarkerRange, digFromMaxY);
    }

    public TestQuarryConfig withNoEnergy(boolean noEnergy) {
        return new TestQuarryConfig(debug, noEnergy, powerMap, enableMap, rebornEnergyConversionCoefficient, removeBedrockOnNetherTop, enableChunkLoader, convertDeepslateOres, removeCommonMaterialsByChunkDestroyer, customPlayer, removeMinecartWithChest, markerPlusRange, flexibleMarkerRange, digFromMaxY);
    }

    public TestQuarryConfig withMarkerPlusRange(int markerPlusRange) {
        return new TestQuarryConfig(debug, noEnergy, powerMap, enableMap, rebornEnergyConversionCoefficient, removeBedrockOnNetherTop, enableChunkLoader, convertDeepslateOres, removeCommonMaterialsByChunkDestroyer, customPlayer, removeMinecartWithChest, markerPlusRange, flexibleMarkerRange, digFromMaxY);
    }

    public TestQuarryConfig withFlexibleMarkerRange(int flexibleMarkerRange) {
        return new TestQuarryConfig(debug, noEnergy, powerMap, enableMap, rebornEnergyConversionCoefficient, removeBedrockOnNetherTop, enableChunkLoader, convertDeepslateOres, removeCommonMaterialsByChunkDestroyer, customPlayer, removeMinecartWithChest, markerPlusRange, flexibleMarkerRange, digFromMaxY);
    }

    public TestQuarryConfig withRebornEnergyConversionCoefficient(double rebornEnergyConversionCoefficient) {
        return new TestQuarryConfig(debug, noEnergy, powerMap, enableMap, rebornEnergyConversionCoefficient, removeBedrockOnNetherTop, enableChunkLoader, convertDeepslateOres, removeCommonMaterialsByChunkDestroyer, customPlayer, removeMinecartWithChest, markerPlusRange, flexibleMarkerRange, digFromMaxY);
    }
}
