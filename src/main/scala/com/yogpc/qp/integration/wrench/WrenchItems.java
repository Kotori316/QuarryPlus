package com.yogpc.qp.integration.wrench;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class WrenchItems {
    public static boolean isWrenchItem(ItemStack stack) {
        var item = stack.getItem();
        return item == Items.STICK
            || item == Items.CARROT_ON_A_STICK
            ;
    }

}
