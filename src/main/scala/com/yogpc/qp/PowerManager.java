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
import com.yogpc.qp.tile.EnergyUsage;
import com.yogpc.qp.tile.TileMiningWell;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

@SuppressWarnings("ClassWithTooManyFields")
public class PowerManager {
    private static double QuarryWork_CF, QuarryWork_CS, MiningWell_CF, MiningWell_CS;
    private static double QuarryWork_BP, QuarryWork_CE, QuarryWork_CU, QuarryWork_XR, QuarryWork_MS;// Quarry:BreakBlock
    private static double FrameBuild_BP, FrameBuild_CE, FrameBuild_CU, FrameBuild_XR, FrameBuild_MS;// Quarry:MakeFrame
    private static double MiningWell_BP, MiningWell_CE, MiningWell_CU, MiningWell_XR, MiningWell_MS;// MiningWell
    private static double Laser_BP, Laser_CE, Laser_CU, Laser_XR, Laser_MS, Laser_CF, Laser_CS;// Laser
    private static double Refinery_CE, Refinery_CU, Refinery_XR, Refinery_MS;// Refinery
    private static double PumpFrame_BP, PumpFrame_CU;// Pump:Frame
    private static double PumpDrain_BP, PumpDrain_CU;// Pump:Liquid
    private static double MoveHead_BP, MoveHead_CU;// Quarry:MoveHead

    private static final int length = (Configuration.CATEGORY_GENERAL + Configuration.CATEGORY_SPLITTER).length();

    private static double get(final ConfigCategory c, final String name, final double def) {
        if (c.containsKey(name)) {
            Property prop = c.get(name);
            if (prop.getType() == null) {
                prop = new Property(prop.getName(), prop.getString(), Property.Type.DOUBLE);
                prop.setComment(c.getQualifiedName().substring(length) + Configuration.CATEGORY_SPLITTER + name);
                c.put(name, prop);
            }
            prop.setMinValue(0.1d).setMaxValue(2e9).setDefaultValue(def);
            return prop.getDouble(def);
        }
        final Property prop = new Property(name, Double.toString(def), Property.Type.DOUBLE);
        prop.setComment(c.getQualifiedName().substring(length) + Configuration.CATEGORY_SPLITTER + name);
        prop.setMinValue(0.1d).setMaxValue(2e9).setDefaultValue(def);
        c.put(name, prop);
        return prop.getDouble(def);
    }

    @SuppressWarnings("SpellCheckingInspection")
    static void loadConfiguration(final Configuration cg) throws RuntimeException {
        ConfigCategory powerSetting = cg.getCategory(Configuration.CATEGORY_GENERAL + Configuration.CATEGORY_SPLITTER + "PowerSetting");
        powerSetting.setComment("Quarry PowerSetting (min = 0.1, Max = 2,000,000,000 = 2 billion)");
        final String cn = Configuration.CATEGORY_GENERAL + Configuration.CATEGORY_SPLITTER + "PowerSetting" + Configuration.CATEGORY_SPLITTER;

        String cn2 = cn + "Quarry" + Configuration.CATEGORY_SPLITTER;
        ConfigCategory c = cg.getCategory(cn2 + "BreakBlock");
//        c.setComment("Quarry BreakBlock");
        QuarryWork_BP = get(c, "BasePower", 40);
        QuarryWork_CE = get(c, "EfficiencyCoefficient", 1.3);
        QuarryWork_CU = get(c, "UnbreakingCoefficient", 1);
        QuarryWork_CF = get(c, "FortuneCoefficient", 1.3);
        QuarryWork_CS = get(c, "SilktouchCoefficient", 2);
        QuarryWork_XR = get(c, "BaseMaxRecieve", 300);
        QuarryWork_MS = get(c, "BaseMaxStored", 15000);

        c = cg.getCategory(cn2 + "BreakBlock" + Configuration.CATEGORY_SPLITTER + "MoveHead");
        MoveHead_BP = get(c, "BasePower", 200);
        MoveHead_CU = get(c, "UnbreakingCoefficient", 1);

        c = cg.getCategory(cn2 + "MakeFrame");
        FrameBuild_BP = get(c, "BasePower", 25);
        FrameBuild_CE = get(c, "EfficiencyCoefficient", 1.3);
        FrameBuild_CU = get(c, "UnbreakingCoefficient", 1);
        FrameBuild_XR = get(c, "BaseMaxRecieve", 100);
        FrameBuild_MS = get(c, "BaseMaxStored", 15000);

        cn2 = cn + "Pump" + Configuration.CATEGORY_SPLITTER;
        c = cg.getCategory(cn2 + "DrainLiquid");
        PumpDrain_BP = get(c, "BasePower", 10);
        PumpDrain_CU = get(c, "UnbreakingCoefficient", 1);

        c = cg.getCategory(cn2 + "MakeFrame");
        PumpFrame_BP = get(c, "BasePower", 25);
        PumpFrame_CU = get(c, "UnbreakingCoefficient", 1);

        c = cg.getCategory(cn + "MiningWell");
        MiningWell_BP = get(c, "BasePower", 40);
        MiningWell_CE = get(c, "EfficiencyCoefficient", 1.3);
        MiningWell_CU = get(c, "UnbreakingCoefficient", 1);
        MiningWell_CF = get(c, "FortuneCoefficient", 1.3);
        MiningWell_CS = get(c, "SilktouchCoefficient", 2);
        MiningWell_XR = get(c, "BaseMaxRecieve", 100);
        MiningWell_MS = get(c, "BaseMaxStored", 1000);

        c = cg.getCategory(cn + "Laser");
        Laser_BP = get(c, "BasePower", 4);
        Laser_CE = get(c, "EfficiencyCoefficient", 2);
        Laser_CU = get(c, "UnbreakingCoefficient", 0.1);
        Laser_CF = get(c, "FortuneCoefficient", 1.05);
        Laser_CS = get(c, "SilktouchCoefficient", 1.1);
        Laser_XR = get(c, "BaseMaxRecieve", 100);
        Laser_MS = get(c, "BaseMaxStored", 1000);

        c = cg.getCategory(cn + "Refinery");
        Refinery_CE = get(c, "EfficiencyCoefficient", 1.2);
        Refinery_CU = get(c, "UnbreakingCoefficient", 1);
        Refinery_XR = get(c, "BaseMaxRecieve", 6);
        Refinery_MS = get(c, "BaseMaxStored", 1000);

    }

    public static void configure0(final APowerTile pp) {
        pp.configure(0, pp.getMaxStored());
    }

    private static void configure(final APowerTile pp, final double CE, final byte efficiencyLevel, final byte unbreakingLevel,
                                  final double CU, final double XR, final double MS, final int pump) {
        pp.configure(XR * Math.pow(CE, efficiencyLevel) / (unbreakingLevel * CU + 1),
            MS * Math.pow(CE, efficiencyLevel) / (unbreakingLevel * CU + 1)
                + (pump > 0 ? 65536 * PumpDrain_BP / (pump * PumpDrain_CU + 1) + 1020 * PumpFrame_BP / (pump * PumpFrame_CU + 1) : 0));
    }

    //What???
    /*private static void configure15(final APowerTile pp, final double CE, final byte efficiencyLevel, final byte unbreakingLevel,
                                    final double CU, final double XR, final double MS, final byte pump) {
        pp.configure(XR * Math.pow(CE, efficiencyLevel) / (unbreakingLevel * CU + 1),
                MS * Math.pow(CE, efficiencyLevel) / (unbreakingLevel * CU + 1)
                + (pump > 0 ? 65536 * PumpDrain_BP / (pump * PumpDrain_CU + 1) + 1020 * PumpFrame_BP / (pump * PumpFrame_CU + 1) : 0));
    }*/

    public static void configureQuarryWork(final APowerTile pp, final byte efficiencyLevel, final byte unbreakingLevel, final int pump) {
        configure(pp, QuarryWork_CE, efficiencyLevel, unbreakingLevel, QuarryWork_CU, QuarryWork_XR, QuarryWork_MS, pump);
    }

    public static void configureMiningWell(final APowerTile pp, final byte efficiencyLevel, final byte unbreakingLevel, final int pump) {
        configure(pp, MiningWell_CE, efficiencyLevel, unbreakingLevel, MiningWell_CU, MiningWell_XR, MiningWell_MS, pump);
    }

    public static void configureLaser(final APowerTile pp, final byte efficiencyLevel, final byte unbreakingLevel) {
        configure(pp, Laser_CE, efficiencyLevel, unbreakingLevel, Laser_CU, Laser_XR, Laser_MS, 0);
    }

    public static void configureFrameBuild(final APowerTile pp, final byte efficiencyLevel, final byte unbreakingLevel, final int pump) {
        configure(pp, FrameBuild_CE, efficiencyLevel, unbreakingLevel, FrameBuild_CU, FrameBuild_XR, FrameBuild_MS, pump);
    }

    public static void configureRefinery(final APowerTile pp, final byte efficiencyLevel, final byte unbreakingLevel) {
        configure(pp, Refinery_CE, efficiencyLevel, unbreakingLevel, Refinery_CU, Refinery_XR, Refinery_MS, 0);
    }

    /**
     * @param pp          power tile
     * @param hardness    block hardness
     * @param enchantMode no enchantment -> 0, silktouch -> -1, fortune -> fortune level, break canceled -> -2
     * @param unbreaking  unbreaking level
     * @param replacer    True if replacer is working.
     * @return Whether the tile used energy.
     */
    public static boolean useEnergyBreak(final APowerTile pp, final float hardness, final byte enchantMode, final byte unbreaking, boolean replacer) {
        if (enchantMode == -2)
            return true;
        final double pw = calcEnergyBreak(pp, hardness, enchantMode, unbreaking) * (replacer ? 1.1 : 1);
        if (pp.useEnergy(pw, pw, false, EnergyUsage.BREAK_BLOCK) != pw)
            return false;
        pp.useEnergy(pw, pw, true, EnergyUsage.BREAK_BLOCK);
        return true;
    }

    /**
     * @param pp          power tile
     * @param hardness    block hardness
     * @param enchantMode no enchantment -> 0, silktouch -> -1, fortune -> fortune level
     * @param unbreaking  unbreaking level
     * @return Require energy.
     */
    private static double calcEnergyBreak(APowerTile pp, float hardness, byte enchantMode, byte unbreaking) {
        double BP, CU, CSP;
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

    public static double calcEnergyBreak(float hardness, int enchantMode, int unbreaking) {
        double BP = QuarryWork_BP, CU = QuarryWork_CU, CSP = enchantMode < 0 ? QuarryWork_CS : Math.pow(QuarryWork_CF, enchantMode);
        return BP * hardness * CSP / (unbreaking * CU + 1);
    }

    public static boolean useEnergyPump(final APowerTile pp, final byte U, final long liquidsCount, final long framesToBuild) {
        final double pw = calcEnergyPumpDrain(U, liquidsCount, framesToBuild);
        if (pp.useEnergy(pw, pw, false, EnergyUsage.PUMP_FLUID) != pw)
            return false;
        pp.useEnergy(pw, pw, true, EnergyUsage.PUMP_FLUID);
        return true;
    }

    public static double calcEnergyPumpDrain(int unbreaking, long liquids, long frames) {
        return PumpDrain_BP * liquids / (unbreaking * PumpDrain_CU + 1) + PumpFrame_BP * frames / (unbreaking * PumpFrame_CU + 1);
    }

    private static boolean useEnergy(final APowerTile pp, final double BP, final int U, final double CU, final int E, final double CE, EnergyUsage usage) {
        final double pw = BP / Math.pow(CE, E) / (U * CU + 1);
        if (pp.useEnergy(pw, pw, false, usage) != pw)
            return false;
        pp.useEnergy(pw, pw, true, usage);
        return true;
    }

    public static boolean useEnergyFrameBuild(final APowerTile pp, final int U) {
        return useEnergy(pp, FrameBuild_BP, U, FrameBuild_CU, 0, 1, EnergyUsage.FRAME_BUILD);
    }

    public static boolean useEnergyRefinery(final APowerTile pp, final double BP, final byte U, final byte E) {
        return useEnergy(pp, BP, U, Refinery_CU, E, Refinery_CE, EnergyUsage.REFINERY);
    }

    public static double useEnergyQuarryHead(final APowerTile pp, final double dist, final byte U) {
        double pw;
        if (!Config.content().fastQuarryHeadMove()) {
            pw = Math.min(2 + pp.getStoredEnergy() / 500, (dist / 2 - 0.05) * MoveHead_BP / (U * MoveHead_CU + 1));
        } else {
            pw = (dist / 2 - 0.05) * MoveHead_BP / (U * MoveHead_CU + 1);
        }
        pw = pp.useEnergy(0, pw, true, EnergyUsage.MOVE_HEAD);
        return pw * (U * MoveHead_CU + 1) / MoveHead_BP + 0.05;
    }

    public static double simulateEnergyLaser(final APowerTile pp, final byte U, final byte F, final boolean S, final byte E) {
        double pw = Laser_BP * Math.pow(Laser_CF, F) * Math.pow(Laser_CE, E) / (U * Laser_CU + 1);
        if (S)
            pw *= Laser_CS;
        pw = pp.useEnergy(0, pw, false, EnergyUsage.LASER);
        if (S)
            pw = pw / Laser_CS;
        return pw * (U * Laser_CU + 1) / Math.pow(Laser_CF, F);
    }

    public static void useEnergyLaser(final APowerTile pp, final double power, final byte U, final byte F, final boolean S, boolean simulate) {
        double pw = power * Math.pow(Laser_CF, F) * (S ? Laser_CS : 1) / (U * Laser_CU + 1);
        pp.useEnergy(pw, pw, !simulate, EnergyUsage.LASER);
    }

    /**
     * @return required energy to search.
     */
    public static double calcEnergyAdvSearch(int unbreakingLevel, int targetY) {
        return MoveHead_BP * targetY / (MoveHead_CU * unbreakingLevel + 1) / 4;
    }
}
