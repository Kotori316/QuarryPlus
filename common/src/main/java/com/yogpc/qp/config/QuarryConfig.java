package com.yogpc.qp.config;

import com.yogpc.qp.machine.PowerMap;

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

}
