package com.yogpc.qp.fabric;

import com.yogpc.qp.PlatformAccess;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class ItemAccessFabric implements PlatformAccess.ItemAccess {
    @Override
    public int getBurnTime(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        var burnTime = FuelRegistry.INSTANCE.get(stack.getItem());
        return burnTime == null ? 0 : burnTime;
    }

    @Override
    @NotNull
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return stack.getRecipeRemainder();
    }
}
