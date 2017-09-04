package com.yogpc.qp.compat;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ElectricItemManager {
    public static double getCharge(final ItemStack is) {
        final NBTTagCompound c = is.getTagCompound();
        if (c == null)
            return 0;
        return c.getDouble("yog_charge");
    }

    public static double charge(final ItemStack is, final double a, final double x) {
        return set(is, a, x);
    }

    public static double discharge(final ItemStack is, final double a, final double x) {
        return -set(is, -a, x);
    }

    private static double set(final ItemStack is, final double a, final double x) {
        if (a < 0.0D || is.getCount() > 1)
            return 0;
        NBTTagCompound tc = is.getTagCompound();
        if (tc == null)
            is.setTagCompound(tc = new NBTTagCompound());
        final double c = tc.getDouble("yog_charge");
        final double v = Math.max(Math.min(x, c + a), 0);
        if (v > 0.0D)
            tc.setDouble("yog_charge", v);
        else {
            tc.removeTag("yog_charge");
            if (tc.hasNoTags())
                is.setTagCompound(null);
        }
        is.setItemDamage(is.getMaxDamage() > 2 ? is.getMaxDamage() - 1 - (int) (v / x * (is.getMaxDamage() - 2)) : 0);
        return v - c;
    }
}
