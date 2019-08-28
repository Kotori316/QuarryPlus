package com.yogpc.qp.machines.base;

import javax.annotation.Nonnull;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.INameable;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;

public interface HasInv extends net.minecraft.inventory.IInventory , INameable {

    @Override
    default void openInventory(PlayerEntity player) {
    }

    @Override
    default boolean hasCustomName() {
        return false;
    }

    @Override
    default int getSizeInventory() {
        return 1;
    }

    @Override
    default boolean isEmpty() {
        return true;
    }

    @Override
    default ItemStack getStackInSlot(int index) {
        return ItemStack.EMPTY;
    }

    @Override
    default ItemStack decrStackSize(int index, int count) {
        return ItemStack.EMPTY;
    }

    @Override
    default ItemStack removeStackFromSlot(int index) {
        return ItemStack.EMPTY;
    }

    @Override
    default void setInventorySlotContents(int index, ItemStack stack) {
    }

    @Override
    default int getInventoryStackLimit() {
        return 0;
    }

    @Override
    default boolean isUsableByPlayer(PlayerEntity player) {
        return true;
    }

    @Override
    default boolean isItemValidForSlot(int index, ItemStack stack) {
        return false;
    }

    @Override
    default void clear() {
    }

    default IItemHandlerModifiable createHandler() {
        return new InvWrapper(this) {
            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                return stack;
            }
        };
    }
}
