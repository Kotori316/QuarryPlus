package com.yogpc.qp;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class CreativeTabQuarryPlus extends CreativeTabs {

    public CreativeTabQuarryPlus() {
        super(QuarryPlus.Mod_Name);
    }

    @Override
    public ItemStack getTabIconItem() {
        return new ItemStack(QuarryPlusI.blockQuarry);
    }
}
