package com.yogpc.qp.version;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public interface VersionDiff {
    ItemStack empty();

    boolean isEmpty(ItemStack stack);

    default boolean nonEmpty(ItemStack stack) {
        return !isEmpty(stack);
    }

    void sendWindowProperty(IContainerListener listener, Container containerIn, int varToUpdate, int newValue);

    void onTake(Slot slot, EntityPlayer thePlayer, ItemStack stack);
}
