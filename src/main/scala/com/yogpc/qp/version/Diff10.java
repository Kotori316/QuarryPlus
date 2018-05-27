package com.yogpc.qp.version;

import java.util.Iterator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;

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
    public void setCountForce(ItemStack stack, int newSize) {
        if (stack != null) {
            stack.stackSize = newSize;
        }
    }

    @Override
    public void sendWindowProperty(IContainerListener listener, Container containerIn, int varToUpdate, int newValue) {
        listener.sendProgressBarUpdate(containerIn, varToUpdate, newValue);
    }

    @Override
    public void onTake(Slot slot, EntityPlayer thePlayer, ItemStack stack) {
        slot.onPickupFromSlot(thePlayer, stack);
    }

    @Override
    public Iterator<NBTBase> getIterator(NBTTagList list) {
        return nbtListStream(list).map(NBTBase.class::cast).iterator();
    }

    @Override
    public Stream<NBTTagCompound> nbtListStream(NBTTagList list) {
        if (list == null) {
            return Stream.empty();
        } else {
            return IntStream.range(0, list.tagCount()).mapToObj(list::getCompoundTagAt);
        }
    }

    @Override
    public void sendMessage(EntityPlayer player, ITextComponent component, boolean b) {
        player.addChatComponentMessage(component);
    }
}
