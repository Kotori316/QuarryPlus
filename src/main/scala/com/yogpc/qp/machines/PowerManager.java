package com.yogpc.qp.machines;

public class PowerManager {
    // Base Energy
    private static final long MAKE_FRAME = PowerTile.ONE_FE * 15;
    private static final long BREAK_BLOCK_BASE = PowerTile.ONE_FE * 25;
    private static final long BREAK_BLOCK_FLUID = BREAK_BLOCK_BASE * 5;
    private static final long MOVE_HEAD_BASE = PowerTile.ONE_FE;
    private static final long EXP_COLLECT = BREAK_BLOCK_BASE / 10;

    // Coefficient
    private static final double FIFTH_ROOT_OF_10 = 1.5848931924611136;
    private static final double FIFTH_ROOT_OF_5 = 1.379729661461215;
    private static final double FORTUNE_COEFFICIENT = 1.5874010519681994;
    private static final double SILKTOUCH_COEFFICIENT = 4;

    public static long getMakeFrameEnergy(EnchantmentLevel.HasEnchantments enchantments) {
        return MAKE_FRAME / (1 + Math.max(0, enchantments.unbreakingLevel()));
    }

    public static long getMoveEnergy(double distance, EnchantmentLevel.HasEnchantments enchantments) {
        double distanceEnergy = distance * MOVE_HEAD_BASE;
        double efficiencyBalanced = Math.pow(FIFTH_ROOT_OF_10, enchantments.efficiencyLevel()) * distanceEnergy;
        return (long) efficiencyBalanced / (1 + Math.max(0, enchantments.unbreakingLevel()));
    }

    /**
     * Use quarry's values. (BasePower, CoEfficiency, and so on.)
     *
     * @return Energy required to break the block. (Micro MJ)
     */
    public static long getBreakEnergy(float hardness, int efficiency, int fortune, int unbreaking, boolean silktouch) {
        if (hardness < 0 || Float.isInfinite(hardness)) return 200 * BREAK_BLOCK_BASE * (efficiency + 1);
        double coefficient = ((double) hardness) / (1 + Math.max(0, unbreaking));
        coefficient *= Math.pow(FORTUNE_COEFFICIENT, fortune);
        if (silktouch) coefficient *= SILKTOUCH_COEFFICIENT;
        coefficient *= Math.pow(FIFTH_ROOT_OF_5, efficiency);

        return (long) (coefficient * BREAK_BLOCK_BASE);
    }

    /**
     * @return Energy required to break the block. (Micro MJ)
     */
    public static long getBreakEnergy(float hardness, EnchantmentLevel.HasEnchantments enchantments) {
        return getBreakEnergy(hardness, enchantments.efficiencyLevel(), enchantments.fortuneLevel(),
            enchantments.unbreakingLevel(), enchantments.silktouchLevel() > 0);
    }

    public static long getBreakBlockFluidEnergy(EnchantmentLevel.HasEnchantments enchantments) {
        return BREAK_BLOCK_FLUID / (1 + Math.max(0, enchantments.unbreakingLevel()));
    }

    public static long getExpCollectEnergy(int exp, EnchantmentLevel.HasEnchantments enchantments) {
        return EXP_COLLECT * exp * 2 / (2 + Math.max(enchantments.unbreakingLevel(), 0));
    }
}
