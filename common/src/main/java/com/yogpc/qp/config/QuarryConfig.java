package com.yogpc.qp.config;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.yogpc.qp.QuarryLogger;
import com.yogpc.qp.machine.PowerMap;
import org.jetbrains.annotations.NotNull;

public interface QuarryConfig {
    boolean debug();

    boolean noEnergy();

    PowerMap powerMap();

    EnableMap enableMap();

    double rebornEnergyConversionCoefficient();

    boolean removeBedrockOnNetherTop();

    boolean enableChunkLoader();

    boolean convertDeepslateOres();

    boolean removeCommonMaterialsByChunkDestroyer();

    boolean customPlayer();

    boolean removeMinecartWithChest();

    int markerPlusRange();

    int flexibleMarkerRange();

    boolean digFromMaxY();

    static void onConfigLoad(@NotNull QuarryConfig newConfig) {
        QuarryLogger.applyLogLevel(newConfig.debug());
        QuarryLogger.LOGGER.info(
            QuarryLogger.LOG4J_CONFIG,
            QuarryConfigLoader.CODEC.codec()
                .encodeStart(JsonOps.INSTANCE, newConfig)
                .map(JsonElement::toString)
                .resultOrPartial()
                .orElse("Failed to convert config to JSON")
        );
    }
}
