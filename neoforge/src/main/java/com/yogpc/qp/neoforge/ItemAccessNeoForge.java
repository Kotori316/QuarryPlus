package com.yogpc.qp.neoforge;

import com.yogpc.qp.PlatformAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public final class ItemAccessNeoForge implements PlatformAccess.ItemAccess {
    @Override
    public int getBurnTime(ItemStack stack, Level level) {
        return stack.getBurnTime(RecipeType.SMELTING, level.fuelValues());
    }

    @Override
    @NotNull
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        var remainder = stack.getCraftingRemainder();
        return remainder == null ? ItemStack.EMPTY : remainder.create();
    }
}
