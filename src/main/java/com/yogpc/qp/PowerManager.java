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
import com.yogpc.qp.tile.TileMiningWell;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class PowerManager {
    private static double B_CF, B_CS, W_CF, W_CS;
    private static double B_BP, B_CE, B_CU, B_XR, B_MS;// Quarry:BreakBlock
    private static double F_BP, F_CE, F_CU, F_XR, F_MS;// Quarry:MakeFrame
    private static double W_BP, W_CE, W_CU, W_XR, W_MS;// MiningWell
    private static double L_BP, L_CE, L_CU, L_XR, L_MS, L_CF, L_CS;// Laser
    private static double R_CE, R_CU, R_XR, R_MS;// Refinery
    private static double PF_BP, PF_CU;// Pump:Frame
    private static double PL_BP, PL_CU;// Pump:Liquid
    private static double H_BP, H_CU;// Quarry:MoveHead

    private static double get(final ConfigCategory c, final String name, final double def) {
        if (c.containsKey(name)) {
            Property prop = c.get(name);
            if (prop.getType() == null) {
                prop = new Property(prop.getName(), prop.getString(), Property.Type.DOUBLE);
                c.put(name, prop);
            }
            return prop.getDouble(def);
        }
        final Property prop = new Property(name, Double.toString(def), Property.Type.DOUBLE);
        c.put(name, prop);
        return prop.getDouble(def);
    }

    static void loadConfiguration(final Configuration cg) throws RuntimeException {
        final String cn = Configuration.CATEGORY_GENERAL + Configuration.CATEGORY_SPLITTER + "PowerSetting" + Configuration.CATEGORY_SPLITTER;

        String cn2 = cn + "Quarry" + Configuration.CATEGORY_SPLITTER;
        ConfigCategory c = cg.getCategory(cn2 + "BreakBlock");
        B_BP = get(c, "BasePower", 40);
        B_CE = get(c, "EfficiencyCoefficient", 1.3);
        B_CU = get(c, "UnbreakingCoefficient", 1);
        B_CF = get(c, "FortuneCoefficient", 1.3);
        B_CS = get(c, "SilktouchCoefficient", 2);
        B_XR = get(c, "BaseMaxRecieve", 300);
        B_MS = get(c, "BaseMaxStored", 15000);

        c = cg.getCategory(cn2 + "BreakBlock" +
                Configuration.CATEGORY_SPLITTER + "MoveHead");
        H_BP = get(c, "BasePower", 200);
        H_CU = get(c, "UnbreakingCoefficient", 1);

        c = cg.getCategory(cn2 + "MakeFrame");
        F_BP = get(c, "BasePower", 25);
        F_CE = get(c, "EfficiencyCoefficient", 1.3);
        F_CU = get(c, "UnbreakingCoefficient", 1);
        F_XR = get(c, "BaseMaxRecieve", 100);
        F_MS = get(c, "BaseMaxStored", 15000);

        cn2 = cn + "Pump" + Configuration.CATEGORY_SPLITTER;
        c = cg.getCategory(cn2 + "DrainLiquid");
        PL_BP = get(c, "BasePower", 10);
        PL_CU = get(c, "UnbreakingCoefficient", 1);

        c = cg.getCategory(cn2 + "MakeFrame");
        PF_BP = get(c, "BasePower", 25);
        PF_CU = get(c, "UnbreakingCoefficient", 1);

        c = cg.getCategory(cn + "MiningWell");
        W_BP = get(c, "BasePower", 40);
        W_CE = get(c, "EfficiencyCoefficient", 1.3);
        W_CU = get(c, "UnbreakingCoefficient", 1);
        W_CF = get(c, "FortuneCoefficient", 1.3);
        W_CS = get(c, "SilktouchCoefficient", 2);
        W_XR = get(c, "BaseMaxRecieve", 100);
        W_MS = get(c, "BaseMaxStored", 1000);

        c = cg.getCategory(cn + "Laser");
        L_BP = get(c, "BasePower", 4);
        L_CE = get(c, "EfficiencyCoefficient", 2);
        L_CU = get(c, "UnbreakingCoefficient", 0.1);
        L_CF = get(c, "FortuneCoefficient", 1.05);
        L_CS = get(c, "SilktouchCoefficient", 1.1);
        L_XR = get(c, "BaseMaxRecieve", 100);
        L_MS = get(c, "BaseMaxStored", 1000);

        c = cg.getCategory(cn + "Refinery");
        R_CE = get(c, "EfficiencyCoefficient", 1.3);
        R_CU = get(c, "UnbreakingCoefficient", 1);
        R_XR = get(c, "BaseMaxRecieve", 100);
        R_MS = get(c, "BaseMaxStored", 1000);

        final StringBuilder sb = new StringBuilder();

        if (B_BP < 0)
            sb.append("general.PowerSetting.Quarry.BreakBlock.BasePower value is bad.\n");
        if (B_CE < 0)
            sb.append("general.PowerSetting.Quarry.BreakBlock.EfficiencyCoefficient value is bad.\n");
        if (B_CU < 0)
            sb.append("general.PowerSetting.Quarry.BreakBlock.UnbreakingCoefficient value is bad.\n");
        if (B_CF < 0)
            sb.append("general.PowerSetting.Quarry.BreakBlock.FortuneCoefficient value is bad.\n");
        if (B_CS < 0)
            sb.append("general.PowerSetting.Quarry.BreakBlock.SilktouchCoefficient value is bad.\n");
        if (B_MS <= 0)
            sb.append("general.PowerSetting.Quarry.BreakBlock.BaseMaxStored value is bad.\n");

        if (H_BP < 0)
            sb.append("general.PowerSetting.Quarry.BreakBlock.MoveHead.BasePower value is bad.\n");
        if (H_CU < 0)
            sb.append("general.PowerSetting.Quarry.BreakBlock.MoveHead.UnbreakingCoefficient value is bad.\n");

        if (F_BP < 0)
            sb.append("general.PowerSetting.Quarry.MakeFrame.BasePower value is bad.\n");
        if (F_CE < 0)
            sb.append("general.PowerSetting.Quarry.MakeFrame.EfficiencyCoefficient value is bad.\n");
        if (F_CU < 0)
            sb.append("general.PowerSetting.Quarry.MakeFrame.UnbreakingCoefficient value is bad.\n");
        if (F_MS <= 0)
            sb.append("general.PowerSetting.Quarry.MakeFrame.BaseMaxStored value is bad.\n");

        if (PL_BP < 0)
            sb.append("general.PowerSetting.Pump.DrainLiquid.BasePower value is bad.\n");
        if (PL_CU < 0)
            sb.append("general.PowerSetting.Pump.DrainLiquid.UnbreakingCoefficient value is bad.\n");

        if (PF_BP < 0)
            sb.append("general.PowerSetting.Pump.MakeFrame.BasePower value is bad.\n");
        if (PF_CU < 0)
            sb.append("general.PowerSetting.Pump.MakeFrame.UnbreakingCoefficient value is bad.\n");

        if (W_BP < 0)
            sb.append("general.PowerSetting.MiningWell.BasePower value is bad.\n");
        if (W_CE < 0)
            sb.append("general.PowerSetting.MiningWell.EfficiencyCoefficient value is bad.\n");
        if (W_CU < 0)
            sb.append("general.PowerSetting.MiningWell.UnbreakingCoefficient value is bad.\n");
        if (W_CF < 0)
            sb.append("general.PowerSetting.MiningWell.FortuneCoefficient value is bad.\n");
        if (W_CS < 0)
            sb.append("general.PowerSetting.MiningWell.SilktouchCoefficient value is bad.\n");
        if (W_MS <= 0)
            sb.append("general.PowerSetting.MiningWell.BaseMaxStored value is bad.\n");

        if (L_BP < 0)
            sb.append("general.PowerSetting.Laser.BasePower value is bad.\n");
        if (L_CE < 0)
            sb.append("general.PowerSetting.Laser.EfficiencyCoefficient value is bad.\n");
        if (L_CU < 0)
            sb.append("general.PowerSetting.Laser.UnbreakingCoefficient value is bad.\n");
        if (L_CF < 0)
            sb.append("general.PowerSetting.Laser.FortuneCoefficient value is bad.\n");
        if (L_CS < 0)
            sb.append("general.PowerSetting.Laser.SilktouchCoefficient value is bad.\n");
        if (L_MS <= 0)
            sb.append("general.PowerSetting.Laser.BaseMaxStored value is bad.\n");

        if (R_CE < 0)
            sb.append("general.PowerSetting.Refinery.EfficiencyCoefficient value is bad.\n");
        if (R_CU < 0)
            sb.append("general.PowerSetting.Refinery.UnbreakingCoefficient value is bad.\n");
        if (R_MS <= 0)
            sb.append("general.PowerSetting.Refinery.BaseMaxStored value is bad.\n");

        if (sb.length() != 0)
            throw new RuntimeException(sb.toString());
    }

    public static void configure0(final APowerTile pp) {
        pp.configure(0, pp.getMaxStored());
    }

    private static void configure(final APowerTile pp, final double CE, final byte E, final byte U,
                                  final double CU, final double XR, final double MS, final byte pump) {
        pp.configure(XR * Math.pow(CE, E) / (U * CU + 1), MS * Math.pow(CE, E) / (U * CU + 1)
                + (pump > 0 ? 65536 * PL_BP / (pump * PL_CU + 1) + 1020 * PF_BP / (pump * PF_CU + 1) : 0));
    }

    private static void configure15(final APowerTile pp, final double CE, final byte E, final byte U,
                                    final double CU, final double XR, final double MS, final byte pump) {
        pp.configure(XR * Math.pow(CE, E) / (U * CU + 1), MS * Math.pow(CE, E) / (U * CU + 1)
                + (pump > 0 ? 65536 * PL_BP / (pump * PL_CU + 1) + 1020 * PF_BP / (pump * PF_CU + 1) : 0));
    }

    public static void configureB(final APowerTile pp, final byte E, final byte U, final byte pump) {
        configure15(pp, B_CE, E, U, B_CU, B_XR, B_MS, pump);
    }

    public static void configureW(final APowerTile pp, final byte E, final byte U, final byte pump) {
        configure15(pp, W_CE, E, U, W_CU, W_XR, W_MS, pump);
    }

    public static void configureL(final APowerTile pp, final byte E, final byte U) {
        configure(pp, L_CE, E, U, L_CU, L_XR, L_MS, (byte) 0);
    }

    public static void configureF(final APowerTile pp, final byte E, final byte U, final byte pump) {
        configure(pp, F_CE, E, U, F_CU, F_XR, F_MS, pump);
    }

    public static void configureR(final APowerTile pp, final byte E, final byte U) {
        configure(pp, R_CE, E, U, R_CU, R_XR, R_MS, (byte) 0);
    }

    public static boolean useEnergyB(final APowerTile pp, final float H, final byte SF, final byte U) {
        double BP, CU, CSP;
        if (pp instanceof TileMiningWell) {
            BP = W_BP;
            CU = W_CU;
            if (SF < 0)
                CSP = W_CS;
            else
                CSP = Math.pow(W_CF, SF);
        } else {
            BP = B_BP;
            CU = B_CU;
            if (SF < 0)
                CSP = B_CS;
            else
                CSP = Math.pow(B_CF, SF);
        }
        final double pw = BP * H * CSP / (U * CU + 1);
        if (pp.useEnergy(pw, pw, false) != pw)
            return false;
        pp.useEnergy(pw, pw, true);
        return true;
    }

    public static boolean useEnergyP(final APowerTile pp, final byte U, final long liquids, final long frames) {
        final double pw = PL_BP * liquids / (U * PL_CU + 1) + PF_BP * frames / (U * PF_CU + 1);
        if (pp.useEnergy(pw, pw, false) != pw)
            return false;
        pp.useEnergy(pw, pw, true);
        return true;
    }

    private static boolean useEnergy(final APowerTile pp, final double BP, final byte U, final double CU, final byte E, final double CE) {
        final double pw = BP / Math.pow(CE, E) / (U * CU + 1);
        if (pp.useEnergy(pw, pw, false) != pw)
            return false;
        pp.useEnergy(pw, pw, true);
        return true;
    }

    public static boolean useEnergyF(final APowerTile pp, final byte U) {
        return useEnergy(pp, F_BP, U, F_CU, (byte) 0, 1);
    }

    public static boolean useEnergyR(final APowerTile pp, final double BP, final byte U, final byte E) {
        return useEnergy(pp, BP, U, R_CU, E, R_CE);
    }

    public static double useEnergyH(final APowerTile pp, final double dist, final byte U) {
        double pw = Math.min(2 + pp.getStoredEnergy() / 500, (dist - 0.1) * H_BP / (U * H_CU + 1));
        pw = pp.useEnergy(0, pw, true);
        return pw * (U * H_CU + 1) / H_BP + 0.1;
    }

    public static double useEnergyL(final APowerTile pp, final byte U, final byte F, final boolean S, final byte E) {
        double pw = L_BP * Math.pow(L_CF, F) * Math.pow(L_CE, E) / (U * L_CU + 1);
        if (S)
            pw *= L_CS;
        pw = pp.useEnergy(0, pw, true);
        if (S)
            pw = pw / L_CS;
        return pw * (U * L_CU + 1) / Math.pow(L_CF, F);
    }
}
