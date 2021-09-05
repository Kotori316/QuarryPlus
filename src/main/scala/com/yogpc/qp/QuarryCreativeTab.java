package com.yogpc.qp;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

class QuarryCreativeTab extends CreativeModeTab {
    QuarryCreativeTab() {
        super(QuarryPlus.modID);
    }

    @Override
    public ItemStack makeIcon() {
        return new ItemStack(Holder.BLOCK_QUARRY);
    }
}
