package com.yogpc.qp.version;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public interface VersionDiff {
    ItemStack empty();

    ItemStack fromNBTTag(NBTTagCompound compound);

    boolean isEmpty(ItemStack stack);

    int getCount(ItemStack stack);

    void setCount(ItemStack stack, int newSize);

    default void shrink(ItemStack stack, int size) {
        setCount(stack, getCount(stack) - size);
    }

    default void grow(ItemStack stack, int size) {
        setCount(stack, getCount(stack) + size);
    }

    default boolean nonEmpty(ItemStack stack) {
        return !isEmpty(stack);
    }

    void sendWindowProperty(IContainerListener listener, Container containerIn, int varToUpdate, int newValue);

    void onTake(Slot slot, EntityPlayer thePlayer, ItemStack stack);
}
