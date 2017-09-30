package com.yogpc.qp.version;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class Diff12 implements VersionDiff {

    @Override
    public ItemStack empty() {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isEmpty(ItemStack stack) {
        return stack.isEmpty();
    }

    @Override
    public void sendWindowProperty(IContainerListener listener, Container containerIn, int varToUpdate, int newValue) {
        listener.sendWindowProperty(containerIn, varToUpdate, newValue);
    }

    @Override
    public void onTake(Slot slot, EntityPlayer thePlayer, ItemStack stack) {
        slot.onTake(thePlayer, stack);
    }
}
