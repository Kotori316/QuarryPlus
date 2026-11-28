package com.yogpc.qp.fabric;

import com.yogpc.qp.PlatformAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public final class ItemAccessFabric implements PlatformAccess.ItemAccess {
    @Override
    public int getBurnTime(ItemStack stack, Level level) {
        if (stack.isEmpty()) return 0;
        return level.fuelValues().burnDuration(stack);
    }

    @Override
    @NotNull
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return stack.getItem().getCraftingRemainder();
    }
}
