package com.yogpc.qp.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yogpc.qp.machine.PowerMap;

final class QuarryConfigLoader {
    record QuarryConfigImpl(
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
    }

    static final MapCodec<QuarryConfig> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Codec.BOOL.fieldOf("debug").forGetter(QuarryConfig::debug),
        Codec.BOOL.fieldOf("noEnergy").forGetter(QuarryConfig::noEnergy),
        PowerMap.CODEC.fieldOf("powerMap").forGetter(QuarryConfig::powerMap),
        EnableMap.CODEC.fieldOf("enableMap").forGetter(QuarryConfig::enableMap),
        Codec.DOUBLE.fieldOf("rebornEnergyConversionCoefficient").forGetter(QuarryConfig::rebornEnergyConversionCoefficient),
        Codec.BOOL.fieldOf("removeBedrockOnNetherTop").forGetter(QuarryConfig::removeBedrockOnNetherTop),
        Codec.BOOL.fieldOf("enableChunkLoader").forGetter(QuarryConfig::enableChunkLoader),
        Codec.BOOL.fieldOf("convertDeepslateOres").forGetter(QuarryConfig::convertDeepslateOres),
        Codec.BOOL.fieldOf("removeCommonMaterialsByChunkDestroyer").forGetter(QuarryConfig::removeCommonMaterialsByChunkDestroyer),
        Codec.BOOL.fieldOf("customPlayer").forGetter(QuarryConfig::customPlayer),
        Codec.BOOL.fieldOf("removeMinecartWithChest").forGetter(QuarryConfig::removeMinecartWithChest),
        Codec.INT.fieldOf("markerPlusRange").forGetter(QuarryConfig::markerPlusRange),
        Codec.INT.fieldOf("flexibleMarkerRange").forGetter(QuarryConfig::flexibleMarkerRange),
        Codec.BOOL.fieldOf("digFromMaxY").forGetter(QuarryConfig::digFromMaxY)
    ).apply(i, QuarryConfigImpl::new));

}
