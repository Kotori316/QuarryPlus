package com.yogpc.qp.forge;

import com.yogpc.qp.PlatformAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public final class ItemAccessForge implements PlatformAccess.ItemAccess {
    @Override
    public int getBurnTime(ItemStack stack, Level level) {
        return level.fuelValues().burnDuration(stack, RecipeType.SMELTING);
    }

    @Override
    @NotNull
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        var remainder = stack.getCraftingRemainder();
        return remainder == null ? ItemStack.EMPTY : remainder.create();
    }
}
