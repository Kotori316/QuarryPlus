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

package com.yogpc.qp;

import com.yogpc.qp.tile.APowerTile;
import com.yogpc.qp.tile.DetailDataCollector;
import com.yogpc.qp.tile.EnergyUsage;
import com.yogpc.qp.tile.TileMiningWell;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

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
    private static long FillerWork_BP; // Filler:Base

    private static final int length = (Configuration.CATEGORY_GENERAL + Configuration.CATEGORY_SPLITTER).length();

    private static double get(final ConfigCategory c, final String name, final double def) {
        if (c.containsKey(name)) {
            Property prop = c.get(name);
            if (prop.getType() == null) {
                prop = new Property(prop.getName(), prop.getString(), Property.Type.DOUBLE);
                prop.setComment(c.getQualifiedName().substring(length) + Configuration.CATEGORY_SPLITTER + name);
                c.put(name, prop);
            }
            prop.setMinValue(1e-9).setMaxValue(2e9).setDefaultValue(def);
            return prop.getDouble(def);
        }
        final Property prop = new Property(name, Double.toString(def), Property.Type.DOUBLE);
        prop.setComment(c.getQualifiedName().substring(length) + Configuration.CATEGORY_SPLITTER + name);
        prop.setMinValue(1e-9).setMaxValue(2e9).setDefaultValue(def);
        c.put(name, prop);
        return prop.getDouble(def);
    }

    @SuppressWarnings("SpellCheckingInspection")
    static void loadConfiguration(final Configuration cg) throws RuntimeException {
        ConfigCategory powerSetting = cg.getCategory(Configuration.CATEGORY_GENERAL + Configuration.CATEGORY_SPLITTER + "PowerSetting");
        powerSetting.setComment("Quarry PowerSetting (min = 1e-9, Max = 2,000,000,000 = 2 billion)");
        final String cn = Configuration.CATEGORY_GENERAL + Configuration.CATEGORY_SPLITTER + "PowerSetting" + Configuration.CATEGORY_SPLITTER;

        String cn2 = cn + "Quarry" + Configuration.CATEGORY_SPLITTER;
        ConfigCategory c = cg.getCategory(cn2 + "BreakBlock");
//        c.setComment("Quarry BreakBlock");
        long micro = APowerTile.MJToMicroMJ;
        QuarryWork_BP = (long) (micro * get(c, "BasePower", 40));
        QuarryWork_CE = get(c, "EfficiencyCoefficient", 1.3);
        QuarryWork_CU = get(c, "UnbreakingCoefficient", 1);
        QuarryWork_CF = get(c, "FortuneCoefficient", 1.3);
        QuarryWork_CS = get(c, "SilktouchCoefficient", 2);
        QuarryWork_XR = (long) (micro * get(c, "BaseMaxRecieve", 300));
        QuarryWork_MS = (long) (micro * get(c, "BaseMaxStored", 15000));

        c = cg.getCategory(cn2 + "BreakBlock" + Configuration.CATEGORY_SPLITTER + "MoveHead");
        MoveHead_BP = (long) (micro * get(c, "BasePower", 200));
        MoveHead_CU = get(c, "UnbreakingCoefficient", 1);

        c = cg.getCategory(cn2 + "MakeFrame");
        FrameBuild_BP = (long) (micro * get(c, "BasePower", 25));
        FrameBuild_CE = get(c, "EfficiencyCoefficient", 1.3);
        FrameBuild_CU = get(c, "UnbreakingCoefficient", 1);
        FrameBuild_XR = (long) (micro * get(c, "BaseMaxRecieve", 100));
        FrameBuild_MS = (long) (micro * get(c, "BaseMaxStored", 15000));

        cn2 = cn + "Pump" + Configuration.CATEGORY_SPLITTER;
        c = cg.getCategory(cn2 + "DrainLiquid");
        PumpDrain_BP = (long) (micro * get(c, "BasePower", 10));
        PumpDrain_CU = get(c, "UnbreakingCoefficient", 1);

        c = cg.getCategory(cn2 + "MakeFrame");
        PumpFrame_BP = (long) (micro * get(c, "BasePower", 25));
        PumpFrame_CU = get(c, "UnbreakingCoefficient", 1);

        c = cg.getCategory(cn + "MiningWell");
        MiningWell_BP = (long) (micro * get(c, "BasePower", 40));
        MiningWell_CE = get(c, "EfficiencyCoefficient", 1.3);
        MiningWell_CU = get(c, "UnbreakingCoefficient", 1);
        MiningWell_CF = get(c, "FortuneCoefficient", 1.3);
        MiningWell_CS = get(c, "SilktouchCoefficient", 2);
        MiningWell_XR = (long) (micro * get(c, "BaseMaxRecieve", 100));
        MiningWell_MS = (long) (micro * get(c, "BaseMaxStored", 1000));

        c = cg.getCategory(cn + "Laser");
        Laser_BP = (long) (micro * get(c, "BasePower", 4));
        Laser_CE = get(c, "EfficiencyCoefficient", 2);
        Laser_CU = get(c, "UnbreakingCoefficient", 0.1);
        Laser_CF = get(c, "FortuneCoefficient", 1.05);
        Laser_CS = get(c, "SilktouchCoefficient", 1.1);
        Laser_XR = (long) (micro * get(c, "BaseMaxRecieve", 100));
        Laser_MS = (long) (micro * get(c, "BaseMaxStored", 1000));

        c = cg.getCategory(cn + "Refinery");
        Refinery_CE = get(c, "EfficiencyCoefficient", 1.2);
        Refinery_CU = get(c, "UnbreakingCoefficient", 1);
        Refinery_XR = (long) (micro * get(c, "BaseMaxRecieve", 6));
        Refinery_MS = (long) (micro * get(c, "BaseMaxStored", 1000));

        c = cg.getCategory(cn + "Filler");
        FillerWork_BP = (long) (micro * get(c, "BasePower", 40));
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
     * Consume energy.
     *
     * @param pp     the machine which used the energy.
     * @param energy amount of energy. Unit is micro MJ.
     * @param usage  What is this energy used for?
     * @return whether energy is consumed.
     */
    @SuppressWarnings("deprecation")
    public static boolean useEnergy(APowerTile pp, long energy, EnergyUsage usage) {
        if (pp.useEnergy(energy, energy, false, usage) != energy)
            return false;
        pp.useEnergy(energy, energy, true, usage);
        pp.collector.get().addData(new DetailDataCollector.Common(usage, energy));
        return true;
    }

    /**
     * @param pp          power tile
     * @param hardness    block hardness
     * @param enchantMode no enchantment -> 0, silktouch -> -1, fortune -> fortune level, break canceled -> -2
     * @param unbreaking  unbreaking level
     * @param replacer    True if replacer is working.
     * @return Whether the tile used energy.
     */
    @SuppressWarnings("deprecation")
    public static boolean useEnergyBreak(final APowerTile pp, final float hardness, final int enchantMode, final int unbreaking, boolean replacer, IBlockState state) {
        if (enchantMode == -2)
            return true;
        final long pw = (long) (calcEnergyBreak(pp, hardness, enchantMode, unbreaking) * (replacer ? 1.1 : 1));
        if (pp.useEnergy(pw, pw, false, EnergyUsage.BREAK_BLOCK) != pw)
            return false;
        pp.useEnergy(pw, pw, true, EnergyUsage.BREAK_BLOCK);
        pp.collector.get().addData(new DetailDataCollector.Break(state, hardness, pw));
        return true;
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
     */
    public static long calcEnergyBreak(float hardness, int enchantMode, int unbreaking) {
        long BP = QuarryWork_BP;
        double CU = QuarryWork_CU;
        double CSP = enchantMode < 0 ? QuarryWork_CS : Math.pow(QuarryWork_CF, enchantMode);
        return (long) (BP * hardness * CSP / (unbreaking * CU + 1));
    }

    @SuppressWarnings("deprecation")
    public static boolean useEnergyPump(final APowerTile pp, final int U, final long liquidsCount, final long framesToBuild) {
        final long pw = calcEnergyPumpDrain(U, liquidsCount, framesToBuild);
        if (pp.useEnergy(pw, pw, false, EnergyUsage.PUMP_FLUID) != pw)
            return false;
        pp.useEnergy(pw, pw, true, EnergyUsage.PUMP_FLUID);
        pp.collector.get().addData(new DetailDataCollector.Pump(liquidsCount, U, framesToBuild, pw));
        return true;
    }

    public static long calcEnergyPumpDrain(int unbreaking, long liquids, long frames) {
        return (long) (PumpDrain_BP * liquids / (unbreaking * PumpDrain_CU + 1) + PumpFrame_BP * frames / (unbreaking * PumpFrame_CU + 1));
    }

    @SuppressWarnings("deprecation")
    private static boolean useEnergy(final APowerTile pp, final long BP, final int U, final double CU, final int E, final double CE, EnergyUsage usage) {
        final long pw = (long) (BP / Math.pow(CE, E) / (U * CU + 1));
        if (pp.useEnergy(pw, pw, false, usage) != pw)
            return false;
        pp.useEnergy(pw, pw, true, usage);
        pp.collector.get().addData(new DetailDataCollector.Common(usage, pw));
        return true;
    }

    public static boolean useEnergyFrameBuild(final APowerTile pp, final int U) {
        return useEnergy(pp, FrameBuild_BP, U, FrameBuild_CU, 0, 1, EnergyUsage.FRAME_BUILD);
    }

    public static boolean useEnergyRefinery(final APowerTile pp, final long BP, final int U, final int E) {
        return useEnergy(pp, BP, U, Refinery_CU, E, Refinery_CE, EnergyUsage.REFINERY);
    }

    @SuppressWarnings("deprecation")
    public static double useEnergyQuarryHead(final APowerTile pp, final double dist, final int U) {
        double bp = (double) MoveHead_BP / APowerTile.MJToMicroMJ;
        double pw;
        if (!Config.content().fastQuarryHeadMove()) {
            pw = Math.min(2 + (double) pp.getStoredEnergy() / 500 / APowerTile.MJToMicroMJ, (dist / 2) * bp / (U * MoveHead_CU + 1));
        } else {
            pw = (dist / 2) * bp / (U * MoveHead_CU + 1);
        }
        long used = pp.useEnergy(0, (long) (pw * APowerTile.MJToMicroMJ), true, EnergyUsage.MOVE_HEAD);
        if (used == 0) return dist;
        pw = (double) used / APowerTile.MJToMicroMJ;
        pp.collector.get().addData(new DetailDataCollector.Common(EnergyUsage.MOVE_HEAD, used));
        return pw * (U * MoveHead_CU + 1) / bp;
    }

    @SuppressWarnings("deprecation")
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

    @SuppressWarnings("deprecation")
    public static void useEnergyLaser(final APowerTile pp, final long power, final int U, final int F, final boolean S, boolean simulate) {
        long pw = (long) (power * Math.pow(Laser_CF, F) * (S ? Laser_CS : 1) / (U * Laser_CU + 1));
        pp.useEnergy(pw, pw, !simulate, EnergyUsage.LASER);
        if (!simulate) pp.collector.get().addData(new DetailDataCollector.Common(EnergyUsage.LASER, pw));
    }

    /**
     * @return required energy to search.
     */
    public static long calcEnergyAdvSearch(int unbreakingLevel, int targetY) {
        return (long) (MoveHead_BP * targetY / (MoveHead_CU * unbreakingLevel + 1) / 4);
    }

    @SuppressWarnings("deprecation")
    public static boolean useEnergyFillerWork(final APowerTile filler, boolean simulate) {
        long pw = FillerWork_BP;
        boolean result = filler.useEnergy(pw, pw, !simulate, EnergyUsage.FILLER) == pw;
        if (result && !simulate) filler.collector.get().addData(new DetailDataCollector.Common(EnergyUsage.FILLER, pw));
        return result;
    }
}
