/*
 * Copyright (C) 2012,2013 yogpstop This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.yogpc.qp.machines;

import com.yogpc.qp.Config;
import com.yogpc.qp.machines.base.APowerTile;
import com.yogpc.qp.machines.base.EnchantmentHolder;
import com.yogpc.qp.machines.base.EnergyUsage;
import com.yogpc.qp.machines.quarry.TileMiningWell;
import net.minecraft.util.math.BlockPos;

@SuppressWarnings("ClassWithTooManyFields")
public class PowerManager {
    private static double QuarryWork_CF, QuarryWork_CS, MiningWell_CF, MiningWell_CS;
    private static long QuarryWork_BP;
    private static double QuarryWork_CE, QuarryWork_CU;
    private static long QuarryWork_XR, QuarryWork_MS;// Quarry:BreakBlock
    private static long FrameBuild_BP;
    private static double FrameBuild_CE, FrameBuild_CU;
    private static long FrameBuild_XR, FrameBuild_MS;// Quarry:MakeFrame
    private static long MiningWell_BP;
    private static double MiningWell_CE, MiningWell_CU;
    private static long MiningWell_XR, MiningWell_MS;// MiningWell
    private static long Laser_BP;
    private static double Laser_CE, Laser_CU;
    private static long Laser_XR, Laser_MS;
    private static double Laser_CF, Laser_CS;// Laser
    private static double Refinery_CE, Refinery_CU;
    private static long Refinery_XR, Refinery_MS;// Refinery
    private static long PumpFrame_BP;
    private static double PumpFrame_CU;// Pump:Frame
    private static long PumpDrain_BP;
    private static double PumpDrain_CU;// Pump:Liquid
    private static long MoveHead_BP;
    private static double MoveHead_CU;// Quarry:MoveHead

    public static void configRegister() {
        long micro = APowerTile.MJToMicroMJ;
        String cat;
        cat = "Quarry.BreakBlock.";
        QuarryWork_BP = (long) (Config.common().powers().apply(cat + "BasePower").get() * micro);
        QuarryWork_XR = (long) (Config.common().powers().apply(cat + "BaseMaxReceive").get() * micro);
        QuarryWork_MS = (long) (Config.common().powers().apply(cat + "BaseMaxStored").get() * micro);
        QuarryWork_CE = Config.common().powers().apply(cat + "EfficiencyCoefficient").get();
        QuarryWork_CU = Config.common().powers().apply(cat + "UnbreakingCoefficient").get();
        QuarryWork_CF = Config.common().powers().apply(cat + "FortuneCoefficient").get();
        QuarryWork_CS = Config.common().powers().apply(cat + "SilktouchCoefficient").get();

        cat = "Quarry.MakeFrame.";
        FrameBuild_BP = (long) (Config.common().powers().apply(cat + "BasePower").get() * micro);
        FrameBuild_XR = (long) (Config.common().powers().apply(cat + "BaseMaxReceive").get() * micro);
        FrameBuild_MS = (long) (Config.common().powers().apply(cat + "BaseMaxStored").get() * micro);
        FrameBuild_CE = Config.common().powers().apply(cat + "EfficiencyCoefficient").get();
        FrameBuild_CU = Config.common().powers().apply(cat + "UnbreakingCoefficient").get();

        cat = "Quarry.BreakBlock.MoveHead.";
        MoveHead_BP = (long) (Config.common().powers().apply(cat + "BasePower").get() * micro);
        MoveHead_CU = Config.common().powers().apply(cat + "UnbreakingCoefficient").get();

        cat = "Pump.DrainLiquid.";
        PumpDrain_BP = (long) (Config.common().powers().apply(cat + "BasePower").get() * micro);
        PumpDrain_CU = Config.common().powers().apply(cat + "UnbreakingCoefficient").get();

        cat = "Pump.MakeFrame.";
        PumpFrame_BP = (long) (Config.common().powers().apply(cat + "BasePower").get() * micro);
        PumpFrame_CU = Config.common().powers().apply(cat + "UnbreakingCoefficient").get();

        cat = "MiningWell.";
        MiningWell_BP = (long) (Config.common().powers().apply(cat + "BasePower").get() * micro);
        MiningWell_XR = (long) (Config.common().powers().apply(cat + "BaseMaxReceive").get() * micro);
        MiningWell_MS = (long) (Config.common().powers().apply(cat + "BaseMaxStored").get() * micro);
        MiningWell_CE = Config.common().powers().apply(cat + "EfficiencyCoefficient").get();
        MiningWell_CU = Config.common().powers().apply(cat + "UnbreakingCoefficient").get();
        MiningWell_CF = Config.common().powers().apply(cat + "FortuneCoefficient").get();
        MiningWell_CS = Config.common().powers().apply(cat + "SilktouchCoefficient").get();

        cat = "Refinery.";
        Refinery_XR = (long) (Config.common().powers().apply(cat + "BaseMaxReceive").get() * micro);
        Refinery_MS = (long) (Config.common().powers().apply(cat + "BaseMaxStored").get() * micro);
        Refinery_CE = Config.common().powers().apply(cat + "EfficiencyCoefficient").get();
        Refinery_CU = Config.common().powers().apply(cat + "UnbreakingCoefficient").get();

        cat = "Laser.";
        Laser_BP = (long) (Config.common().powers().apply(cat + "BasePower").get() * micro);
        Laser_XR = (long) (Config.common().powers().apply(cat + "BaseMaxReceive").get() * micro);
        Laser_MS = (long) (Config.common().powers().apply(cat + "BaseMaxStored").get() * micro);
        Laser_CE = Config.common().powers().apply(cat + "EfficiencyCoefficient").get();
        Laser_CU = Config.common().powers().apply(cat + "UnbreakingCoefficient").get();
        Laser_CF = Config.common().powers().apply(cat + "FortuneCoefficient").get();
        Laser_CS = Config.common().powers().apply(cat + "SilktouchCoefficient").get();
    }

    public static void configure0(final APowerTile pp) {
        pp.configure(0, pp.getMaxStored());
    }

    private static void configure(final APowerTile pp, final double CE, final int efficiencyLevel, final int unbreakingLevel,
                                  final double CU, final long maxReceive, final long maxStored, final int pump) {
        pp.configure((long) (maxReceive * Math.pow(CE, efficiencyLevel) / (unbreakingLevel * CU + 1)),
            (long) (maxStored * Math.pow(CE, efficiencyLevel) / (unbreakingLevel * CU + 1)
                + (pump > 0 ? 65536 * PumpDrain_BP / (pump * PumpDrain_CU + 1) + 1020 * PumpFrame_BP / (pump * PumpFrame_CU + 1) : 0)));
    }

    //What???
    /*private static void configure15(final APowerTile pp, final double CE, final int efficiencyLevel, final int unbreakingLevel,
                                    final double CU, final double XR, final double MS, final int pump) {
        pp.configure_double(XR * Math.pow(CE, efficiencyLevel) / (unbreakingLevel * CU + 1),
                MS * Math.pow(CE, efficiencyLevel) / (unbreakingLevel * CU + 1)
                + (pump > 0 ? 65536 * PumpDrain_BP / (pump * PumpDrain_CU + 1) + 1020 * PumpFrame_BP / (pump * PumpFrame_CU + 1) : 0));
    }*/

    public static void configureQuarryWork(final APowerTile pp, final int efficiencyLevel, final int unbreakingLevel, final int pump) {
        configure(pp, QuarryWork_CE, efficiencyLevel, unbreakingLevel, QuarryWork_CU, QuarryWork_XR, QuarryWork_MS, pump);
    }

    public static void configureMiningWell(final APowerTile pp, final int efficiencyLevel, final int unbreakingLevel, final int pump) {
        configure(pp, MiningWell_CE, efficiencyLevel, unbreakingLevel, MiningWell_CU, MiningWell_XR, MiningWell_MS, pump);
    }

    public static void configureLaser(final APowerTile pp, final int efficiencyLevel, final int unbreakingLevel) {
        configure(pp, Laser_CE, efficiencyLevel, unbreakingLevel, Laser_CU, Laser_XR, Laser_MS, 0);
    }

    public static void configureFrameBuild(final APowerTile pp, final int efficiencyLevel, final int unbreakingLevel, final int pump) {
        configure(pp, FrameBuild_CE, efficiencyLevel, unbreakingLevel, FrameBuild_CU, FrameBuild_XR, FrameBuild_MS, pump);
    }

    public static void configureRefinery(final APowerTile pp, final int efficiencyLevel, final int unbreakingLevel) {
        configure(pp, Refinery_CE, efficiencyLevel, unbreakingLevel, Refinery_CU, Refinery_XR, Refinery_MS, 0);
    }

    /**
     * @param pp              power tile
     * @param hardness        block hardness
     * @param enchantmentMode no enchantment -> 0, silktouch -> -1, fortune -> fortune level, break canceled -> -2
     * @param unbreaking      unbreaking level
     * @param replacer        True if replacer is working.
     * @return Whether the tile used energy.
     */
    public static boolean useEnergyBreak(final APowerTile pp, final float hardness, final int enchantmentMode, final int unbreaking, boolean replacer) {
        if (enchantmentMode == -2)
            return true;
        final long pw = (long) (calcEnergyBreak(pp, hardness, enchantmentMode, unbreaking) * (replacer ? 1.1 : 1));
        if (pp.useEnergy(pw, pw, false, EnergyUsage.BREAK_BLOCK) != pw)
            return false;
        pp.useEnergy(pw, pw, true, EnergyUsage.BREAK_BLOCK);
        return true;
    }

    /**
     * @param pp          power tile which energy come from.
     * @param pos         the position of block which will be removed.
     * @param enchantment enchantment of the tile.
     * @param replacer    whether tile has replacer module.
     * @return {@code true} if succeeded and {@code false} means there isn't enough energy to break block.
     */
    public static boolean useEnergyBreak(final APowerTile pp, final BlockPos pos, final EnchantmentHolder enchantment, boolean replacer) {
        if (pp.getWorld() != null) {
            float blockHardness = pp.getWorld().getBlockState(pos).getBlockHardness(pp.getWorld(), pos);
            int mode = EnchantmentHolder.enchantmentMode(enchantment);
            return useEnergyBreak(pp, blockHardness, mode, enchantment.unbreaking(), replacer);
        } else {
            return true;
        }
    }

    /**
     * @param pp          power tile
     * @param hardness    block hardness
     * @param enchantMode no enchantment -> 0, silktouch -> -1, fortune -> fortune level
     * @param unbreaking  unbreaking level
     * @return Require energy.
     */
    private static double calcEnergyBreak(APowerTile pp, float hardness, int enchantMode, int unbreaking) {
        long BP;
        double CU;
        double CSP;
        if (pp instanceof TileMiningWell) {
            BP = MiningWell_BP;
            CU = MiningWell_CU;
            CSP = enchantMode < 0 ? MiningWell_CS : Math.pow(MiningWell_CF, enchantMode);
        } else {
            BP = QuarryWork_BP;
            CU = QuarryWork_CU;
            CSP = enchantMode < 0 ? QuarryWork_CS : Math.pow(QuarryWork_CF, enchantMode);
        }
        return BP * hardness * CSP / (unbreaking * CU + 1);
    }

    /**
     * Use quarry's values. (BasePower, CoEfficiency, and so on.)
     *
     * @return Energy required to break such block. (Micro MJ)
     */
    public static long calcEnergyBreak(float hardness, int enchantMode, int unbreaking) {
        long BP = QuarryWork_BP;
        double CU = QuarryWork_CU;
        double CSP = enchantMode < 0 ? QuarryWork_CS : Math.pow(QuarryWork_CF, enchantMode);
        double fixedHardness = Math.pow(hardness, 1.1);
        return (long) (BP * fixedHardness * CSP / (unbreaking * CU + 1));
    }

    /**
     * @return Energy required to break such block. (Micro MJ)
     */
    public static long calcEnergyBreak(float hardness, EnchantmentHolder enchantmentHolder) {
        return calcEnergyBreak(hardness, EnchantmentHolder.enchantmentMode(enchantmentHolder), enchantmentHolder.unbreaking());
    }

    public static boolean useEnergyPump(final APowerTile pp, final int U, final long liquidsCount, final long framesToBuild) {
        final long pw = calcEnergyPumpDrain(U, liquidsCount, framesToBuild);
        if (pp.useEnergy(pw, pw, false, EnergyUsage.PUMP_FLUID) != pw)
            return false;
        pp.useEnergy(pw, pw, true, EnergyUsage.PUMP_FLUID);
        return true;
    }

    public static long calcEnergyPumpDrain(int unbreaking, long liquids, long frames) {
        return (long) (PumpDrain_BP * liquids / (unbreaking * PumpDrain_CU + 1) + PumpFrame_BP * frames / (unbreaking * PumpFrame_CU + 1));
    }

    private static boolean useEnergy(final APowerTile pp, final long BP, final int U, final double CU, final int E, final double CE, EnergyUsage usage) {
        final long pw = (long) (BP / Math.pow(CE, E) / (U * CU + 1));
        if (pp.useEnergy(pw, pw, false, usage) != pw)
            return false;
        pp.useEnergy(pw, pw, true, usage);
        return true;
    }

    public static boolean useEnergyFrameBuild(final APowerTile pp, final int U) {
        return useEnergy(pp, FrameBuild_BP, U, FrameBuild_CU, 0, 1, EnergyUsage.FRAME_BUILD);
    }

    public static boolean useEnergyRefinery(final APowerTile pp, final long BP, final int U, final int E) {
        return useEnergy(pp, BP, U, Refinery_CU, E, Refinery_CE, EnergyUsage.REFINERY);
    }

    public static double useEnergyQuarryHead(final APowerTile pp, final double dist, final int U) {
        double bp = (double) MoveHead_BP / APowerTile.MJToMicroMJ;
        double pw;
        if (!Config.common().fastQuarryHeadMove().get()) {
            pw = Math.min(2 + (double) pp.getStoredEnergy() / 500 / APowerTile.MJToMicroMJ, (dist / 2 - 0.05) * bp / (U * MoveHead_CU + 1));
        } else {
            pw = (dist / 2 - 0.05) * bp / (U * MoveHead_CU + 1);
        }
        pw = (double) pp.useEnergy(0, (long) (pw * APowerTile.MJToMicroMJ), true, EnergyUsage.MOVE_HEAD) / APowerTile.MJToMicroMJ;
        return pw * (U * MoveHead_CU + 1) / bp + 0.05;
    }

    public static long calcEnergyQuarryHead(final APowerTile pp, final double dist, final int U) {
        long bp = MoveHead_BP;
        long pw;
        if (!Config.common().fastQuarryHeadMove().get()) {
            pw = Math.min(2 + pp.getStoredEnergy() / 500, (long) ((dist / 2 - 0.05) * bp / (U * MoveHead_CU + 1)));
        } else {
            pw = (long) ((dist / 2 - 0.05) * bp / (U * MoveHead_CU + 1));
        }
        return pw;
    }

    public static long simulateEnergyLaser(final APowerTile pp, final int U, final int F, final boolean S, final int E) {
        long pw = (long) (Laser_BP * Math.pow(Laser_CF, F) * Math.pow(Laser_CE, E) / (U * Laser_CU + 1));
        if (S) {
            long used = pp.useEnergy(0, (long) (pw * Laser_CS), false, EnergyUsage.LASER);
            return (long) (used / Laser_CS * (U * Laser_CU + 1) / Math.pow(Laser_CF, F));
        } else {
            long used = pp.useEnergy(0, pw, false, EnergyUsage.LASER);
            return (long) (used * (U * Laser_CU + 1) / Math.pow(Laser_CF, F));
        }
    }

    public static void useEnergyLaser(final APowerTile pp, final long power, final int U, final int F, final boolean S, boolean simulate) {
        long pw = (long) (power * Math.pow(Laser_CF, F) * (S ? Laser_CS : 1) / (U * Laser_CU + 1));
        pp.useEnergy(pw, pw, !simulate, EnergyUsage.LASER);
    }

    /**
     * @return required energy to search.
     */
    public static long calcEnergyAdvSearch(int unbreakingLevel, int targetY) {
        return (long) (MoveHead_BP * targetY / (MoveHead_CU * unbreakingLevel + 1) / 4);
    }
}
