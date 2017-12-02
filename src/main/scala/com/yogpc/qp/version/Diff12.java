package com.yogpc.qp.version;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class Diff12 implements VersionDiff {

    @Override
    public ItemStack empty() {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack fromNBTTag(NBTTagCompound compound) {
        return new ItemStack(compound);
    }

    @Override
    public boolean isEmpty(ItemStack stack) {
        return stack.isEmpty();
    }

    @Override
    public int getCount(ItemStack stack) {
        return stack.getCount();
    }

    @Override
    public void setCount(ItemStack stack, int newSize) {
        stack.setCount(newSize);
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
