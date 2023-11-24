package com.yogpc.qp;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.fabricmc.loader.api.FabricLoader;

@Config(name = QuarryPlus.modID)
public class QuarryConfig implements ConfigData {
    private static class Constant {
        @ConfigEntry.Gui.Excluded
        private static final String COMMON_CATEGORY = "default";
        @ConfigEntry.Gui.Excluded
        private static final String COMMON_POWER_CATEGORY = "common.power";
    }

    @ConfigEntry.Gui.CollapsibleObject
    public Common common = new Common();
    @ConfigEntry.Gui.CollapsibleObject
    public Power power = new Power();
    @ConfigEntry.Gui.CollapsibleObject
    public Quarry quarry = new Quarry();
    @ConfigEntry.Gui.CollapsibleObject
    public AdvPump adv_pump = new AdvPump();
    @ConfigEntry.Gui.CollapsibleObject
    public AdvQuarry adv_quarry = new AdvQuarry();
    @ConfigEntry.Gui.CollapsibleObject
    public Filler filler = new Filler();

    public static class Common {
        @ConfigEntry.Category(Constant.COMMON_CATEGORY)
        public boolean debug;
        @ConfigEntry.Category(Constant.COMMON_CATEGORY)
        @ConfigEntry.BoundedDiscrete(min = -128, max = 512)
        public int netherTop;
        @ConfigEntry.Category(Constant.COMMON_CATEGORY)
        @ConfigEntry.Gui.RequiresRestart
        public boolean noEnergy = false;
        @ConfigEntry.Category(Constant.COMMON_CATEGORY)
        @ConfigEntry.Gui.RequiresRestart
        public boolean convertDeepslateOres = false;
        @ConfigEntry.Category(Constant.COMMON_CATEGORY)
        @ConfigEntry.Gui.RequiresRestart
        public boolean removesCommonMaterialAdvQuarry = true;
        @ConfigEntry.Category(Constant.COMMON_CATEGORY)
        public boolean removeFrameAfterQuarryIsRemoved = false;

        public Common() {
            try {
                debug = FabricLoader.getInstance().isDevelopmentEnvironment();
                netherTop = FabricLoader.getInstance().isDevelopmentEnvironment() ? 128 : 127;
            } catch (Throwable ignore) {
                // In test environment.
                debug = true;
                netherTop = 128;
            }
        }
    }

    public static class Power {
        @ConfigEntry.Category(Constant.COMMON_POWER_CATEGORY)
        @ConfigEntry.Gui.RequiresRestart
        public double rebornEnergyConversionCoefficient = 1d / 16d;
        @ConfigEntry.Category(Constant.COMMON_POWER_CATEGORY)
        public int creativeGeneratorGeneration = 1000;
    }

    public static class Quarry {
        // Quarry Energy Config
        @ConfigEntry.Category(Constant.COMMON_POWER_CATEGORY)
        @ConfigEntry.Gui.RequiresRestart
        public double quarryEnergyCapacity = 10000d;
        @ConfigEntry.Category(Constant.COMMON_POWER_CATEGORY)
        public double quarryEnergyMakeFrame = 15d;
        @ConfigEntry.Category(Constant.COMMON_POWER_CATEGORY)
        public double quarryEnergyBreakBlock = 10d;
        @ConfigEntry.Category(Constant.COMMON_POWER_CATEGORY)
        public double quarryEnergyRemoveFluid = quarryEnergyBreakBlock * 5;
        @ConfigEntry.Category(Constant.COMMON_POWER_CATEGORY)
        public double quarryEnergyMoveHead = 0.5d;
    }

    public static class AdvPump {
        // Advanced Pump
        @ConfigEntry.Category(Constant.COMMON_POWER_CATEGORY)
        @ConfigEntry.Gui.RequiresRestart
        public double advPumpEnergyCapacity = 1000d;
        @ConfigEntry.Category(Constant.COMMON_POWER_CATEGORY)
        public double advPumpEnergyRemoveFluid = 20d;
    }

    public static class AdvQuarry {
        // Chunk Destroyer Energy Config
        @ConfigEntry.Category(Constant.COMMON_POWER_CATEGORY)
        @ConfigEntry.Gui.RequiresRestart
        public double advQuarryEnergyCapacity = 50000d;
        @ConfigEntry.Category(Constant.COMMON_POWER_CATEGORY)
        public double advQuarryEnergyMakeFrame = 15d;
        @ConfigEntry.Category(Constant.COMMON_POWER_CATEGORY)
        public double advQuarryEnergyBreakBlock = 10d;
        @ConfigEntry.Category(Constant.COMMON_POWER_CATEGORY)
        public double advQuarryEnergyRemoveFluid = advQuarryEnergyBreakBlock * 5;
        @ConfigEntry.Category(Constant.COMMON_POWER_CATEGORY)
        public double advQuarryEnergyMoveHead = 25d;
    }

    public static class Filler {
        // Filler
        @ConfigEntry.Category(Constant.COMMON_POWER_CATEGORY)
        @ConfigEntry.Gui.RequiresRestart
        public double fillerEnergyCapacity = 1000d;
        @ConfigEntry.Category(Constant.COMMON_POWER_CATEGORY)
        public double fillerEnergyBreakBlock = 10d;
    }
}
