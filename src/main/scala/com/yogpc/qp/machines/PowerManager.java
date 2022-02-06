package com.yogpc.qp.machines;

public class PowerManager {

    public static <T extends EnchantmentLevel.HasEnchantments & PowerConfig.Provider> long getMakeFrameEnergy(T enchantments) {
        return getMakeFrameEnergy(enchantments, enchantments.getPowerConfig());
    }

    public static long getMakeFrameEnergy(EnchantmentLevel.HasEnchantments enchantments, PowerConfig config) {
        return config.makeFrame() / (1 + Math.max(0, enchantments.unbreakingLevel()));
    }

    public static <T extends EnchantmentLevel.HasEnchantments & PowerConfig.Provider> long getMoveEnergy(double distance, T enchantments) {
        return getMoveEnergy(distance, enchantments, enchantments.getPowerConfig());
    }

    public static long getMoveEnergy(double distance, EnchantmentLevel.HasEnchantments enchantments, PowerConfig config) {
        double distanceEnergy = distance * config.moveHeadBase();
        double efficiencyBalanced = Math.pow(config.efficiencyCoefficient(), enchantments.efficiencyLevel()) * distanceEnergy;
        return (long) (efficiencyBalanced / (1 + Math.max(0, enchantments.unbreakingLevel())));
    }

    public static <T extends EnchantmentLevel.HasEnchantments & PowerConfig.Provider> long getAdvSearchEnergy(int blocks, T enchantments) {
        PowerConfig config = enchantments.getPowerConfig();
        long heightEnergy = blocks * config.moveHeadBase() * 50;
        double efficiencyBalanced = Math.pow(config.efficiencyCoefficient(), enchantments.efficiencyLevel()) * heightEnergy;
        return (long) (efficiencyBalanced / (1 + Math.max(0, enchantments.unbreakingLevel())));
    }

    /**
     * Get energy to break a block, respecting its hardness and enchantments of the machine.
     * Use quarry's values. (BasePower, CoEfficiency, and so on.)
     *
     * @return Energy required to break the block. (Micro MJ) <br>
     * Special cases: (left side is hardness)
     * <ul>
     *     <li>NaN(Not a Number, {@link Float#NaN}) -> 0</li>
     *     <li>0 -> 0</li>
     *     <li>-1 -> As hardness = 200</li>
     *     <li>Infinity({@link Float#POSITIVE_INFINITY}, {@link Float#NEGATIVE_INFINITY}) -> Treated as hardness = 200</li>
     * </ul>
     */
    public static long getBreakEnergy(float hardness, EnchantmentLevel.HasEnchantments enchantments, PowerConfig config) {
        if (Float.isNaN(hardness) || hardness == 0) return 0;
        var efficiency = enchantments.efficiencyLevel();
        var fortune = enchantments.fortuneLevel();
        var unbreaking = enchantments.unbreakingLevel();
        var silktouch = enchantments.silktouchLevel() > 0;
        if (hardness < 0 || Float.isInfinite(hardness)) return 200 * config.breakBlockBase() * (efficiency + 1);
        // Base energy, considering Fortune and Silktouch. Efficiency and Unbreaking should be calculated later.
        long base = (long) (config.breakBlockBase() * Math.pow(config.breakFortuneCoefficient(), fortune) * Math.pow(config.breakSilktouchCoefficient(), silktouch ? 1 : 0));
        double coefficient = ((double) hardness) * Math.pow(config.breakEfficiencyCoefficient(), efficiency) / (1 + Math.max(0, unbreaking));

        return (long) (coefficient * base);
    }

    public static <T extends EnchantmentLevel.HasEnchantments & PowerConfig.Provider> long getBreakEnergy(float hardness, T enchantments) {
        return getBreakEnergy(hardness, enchantments, enchantments.getPowerConfig());
    }

    public static long getBreakBlockFluidEnergy(EnchantmentLevel.HasEnchantments enchantments, PowerConfig config) {
        return config.breakBlockFluid() / (1 + Math.max(0, enchantments.unbreakingLevel()));
    }

    public static <T extends EnchantmentLevel.HasEnchantments & PowerConfig.Provider> long getBreakBlockFluidEnergy(T enchantments) {
        return getBreakBlockFluidEnergy(enchantments, enchantments.getPowerConfig());
    }

    public static <T extends EnchantmentLevel.HasEnchantments & PowerConfig.Provider> long getExpCollectEnergy(int exp, T enchantments) {
        PowerConfig config = enchantments.getPowerConfig();
        return config.expCollect() * exp * 2 / (2 + Math.max(enchantments.unbreakingLevel(), 0));
    }

    public static <T extends EnchantmentLevel.HasEnchantments & PowerConfig.Provider> long getMiniQuarryEnergy(T enchantments) {
        return enchantments.getPowerConfig().breakBlockBase() / (1 + Math.max(0, enchantments.unbreakingLevel()));
    }

    public static <T extends PowerConfig.Provider> long getFillerEnergy(T enchantments) {
        return enchantments.getPowerConfig().breakBlockBase();
    }
}
