package com.yogpc.qp.version;

import java.util.Iterator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;

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
        return stack == null || stack.isEmpty();
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

    @Override
    public Iterator<NBTBase> getIterator(NBTTagList list) {
        return list.iterator();
    }

    @Override
    public boolean changeAdvPumpState() {
        return true;
    }

    @Override
    public void sendMessage(EntityPlayer player, ITextComponent component, boolean actionBar) {
        player.sendStatusMessage(component, actionBar);
    }
}
