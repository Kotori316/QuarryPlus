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
    private static final double FORTUNE_COEFFICIENT = 1.5874010519681996;
    private static final double SILKTOUCH_COEFFICIENT = 4;

    public static long getMakeFrameEnergy(EnchantmentLevel.HasEnchantments enchantments) {
        return MAKE_FRAME / (1 + Math.max(0, enchantments.unbreakingLevel()));
    }

    public static long getMoveEnergy(double distance, EnchantmentLevel.HasEnchantments enchantments) {
        double distanceEnergy = distance * MOVE_HEAD_BASE;
        double efficiencyBalanced = Math.pow(FIFTH_ROOT_OF_10, enchantments.efficiencyLevel()) * distanceEnergy;
        return (long) (efficiencyBalanced / (1 + Math.max(0, enchantments.unbreakingLevel())));
    }

    public static long getAdvSearchEnergy(int blocks, EnchantmentLevel.HasEnchantments enchantments) {
        long heightEnergy = blocks * MOVE_HEAD_BASE * 50;
        double efficiencyBalanced = Math.pow(FIFTH_ROOT_OF_10, enchantments.efficiencyLevel()) * heightEnergy;
        return (long) (efficiencyBalanced / (1 + Math.max(0, enchantments.unbreakingLevel())));
    }

    /**
     * Use quarry's values. (BasePower, CoEfficiency, and so on.)
     *
     * @return Energy required to break the block. (Micro MJ) <br>
     * Special cases:
     * <ul>
     *     <li>NaN(Not a Number, {@link Float#NaN}) -> 0</li>
     *     <li>Infinity({@link Float#POSITIVE_INFINITY}, {@link Float#NEGATIVE_INFINITY}) -> Treated as hardness = 200</li>
     *     <li>0 -> 0</li>
     * </ul>
     */
    public static long getBreakEnergy(float hardness, int efficiency, int fortune, int unbreaking, boolean silktouch) {
        if (Float.isNaN(hardness) || hardness == 0) return 0;
        if (hardness < 0 || Float.isInfinite(hardness)) return 200 * BREAK_BLOCK_BASE * (efficiency + 1);
        // Base energy, considering Fortune and Silktouch. Efficiency and Unbreaking should be calculated later.
        long base = (long) (BREAK_BLOCK_BASE * Math.pow(FORTUNE_COEFFICIENT, fortune) * Math.pow(SILKTOUCH_COEFFICIENT, silktouch ? 1 : 0));
        double coefficient = ((double) hardness) * Math.pow(FIFTH_ROOT_OF_5, efficiency) / (1 + Math.max(0, unbreaking));

        return (long) (coefficient * base);
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

    public static long getMiniQuarryEnergy(EnchantmentLevel.HasEnchantments enchantments) {
        return 20 * PowerTile.ONE_FE / (1 + Math.max(0, enchantments.unbreakingLevel()));
    }
}
