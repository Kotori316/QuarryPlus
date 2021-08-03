package com.yogpc.qp;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.loader.api.FabricLoader;

@Config(name = QuarryPlus.modID)
public class QuarryConfig implements ConfigData {
    private static final String COMMON_CATEGORY = "common";
    private static final String COMMON_POWER_CATEGORY = "common.power";
    public static QuarryConfig config = null;

    static void register() {
        AutoConfig.register(QuarryConfig.class, Toml4jConfigSerializer::new);
        config = AutoConfig.getConfigHolder(QuarryConfig.class).getConfig();
    }

    @ConfigEntry.Category(COMMON_CATEGORY)
    public boolean debug = FabricLoader.getInstance().isDevelopmentEnvironment();
    @ConfigEntry.Category(COMMON_CATEGORY)
    @ConfigEntry.BoundedDiscrete(min = Short.MIN_VALUE, max = Short.MAX_VALUE)
    public int netherTop = FabricLoader.getInstance().isDevelopmentEnvironment() ? 128 : 127;

    @ConfigEntry.Category(COMMON_POWER_CATEGORY)
    public double rebornEnergyConversionCoefficient = 1d / 16d;
    @ConfigEntry.Category(COMMON_POWER_CATEGORY)
    public double fastTransferEnergyConversionCoefficient = 1d / 16d;
}
