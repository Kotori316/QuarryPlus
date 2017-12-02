package com.yogpc.qp.version;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class Diff10 implements VersionDiff {

    public Diff10() {
    }

    @Override
    public ItemStack empty() {
        return null;
    }

    @Override
    public ItemStack fromNBTTag(NBTTagCompound compound) {
        return ItemStack.loadItemStackFromNBT(compound);
    }

    @Override
    public boolean isEmpty(ItemStack stack) {
        return stack == empty() || stack.stackSize < 1;
    }

    @Override
    public int getCount(ItemStack stack) {
        return nonEmpty(stack) ? stack.stackSize : 0;
    }

    @Override
    public void setCount(ItemStack stack, int newSize) {
        if (nonEmpty(stack))
            stack.stackSize = newSize;
    }

    @Override
    public void sendWindowProperty(IContainerListener listener, Container containerIn, int varToUpdate, int newValue) {
        listener.sendProgressBarUpdate(containerIn, varToUpdate, newValue);
    }

    @Override
    public void onTake(Slot slot, EntityPlayer thePlayer, ItemStack stack) {
        slot.onPickupFromSlot(thePlayer, stack);
    }
}
