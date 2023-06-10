package com.yogpc.qp.integration.wrench;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ObjectHolder;

public final class WrenchItems {
    public static boolean isWrenchItem(ItemStack stack) {
        var item = stack.getItem();
        return item == Holder.STICK ||
                item == Holder.PIPEZ_WRENCH
                ;
    }

    @SuppressWarnings("SpellCheckingInspection")
    private static class Holder {
        @ObjectHolder(registryName = "minecraft:item", value = "minecraft:stick")
        public static Item STICK = null;
        @ObjectHolder(registryName = "minecraft:item", value = "pipez:wrench")
        public static Item PIPEZ_WRENCH = null;
    }
}
