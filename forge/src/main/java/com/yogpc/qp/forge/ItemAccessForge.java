package com.yogpc.qp.forge;

import com.yogpc.qp.PlatformAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.NotNull;

public final class ItemAccessForge implements PlatformAccess.ItemAccess {
    @SuppressWarnings("UnstableApiUsage")
    @Override
    public int getBurnTime(ItemStack stack) {
        return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING);
    }

    @Override
    @NotNull
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return stack.getCraftingRemainingItem();
    }
}
