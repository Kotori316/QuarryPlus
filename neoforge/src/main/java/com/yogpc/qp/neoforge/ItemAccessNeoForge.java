package com.yogpc.qp.neoforge;

import com.yogpc.qp.PlatformAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;

public final class ItemAccessNeoForge implements PlatformAccess.ItemAccess {
    @Override
    public int getBurnTime(ItemStack stack) {
        return stack.getBurnTime(RecipeType.SMELTING);
    }

    @Override
    @NotNull
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return stack.getCraftingRemainingItem();
    }
}
