package com.yogpc.qp.tile;

import com.yogpc.qp.version.VersionUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface HasInv extends net.minecraft.inventory.IInventory {

    @Override
    default void openInventory(EntityPlayer player) {
    }

    @Override
    default void closeInventory(EntityPlayer player) {
    }

    /**
     * Send short value to client.
     *
     * @param id the index of data. Must be under 256.
     * @return short value, must be under 2^15.
     */
    @Override
    default int getField(int id) {
        return 0;
    }

    /**
     * Get from server and set value in client side.
     *
     * @param id    the index of data. Must be under 256.
     * @param value value in server side. Should be under 2^15.
     */
    @Override
    default void setField(int id, int value) {
    }

    @Override
    default int getFieldCount() {
        return 0;
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
        return VersionUtil.empty();
    }

    @Override
    default ItemStack decrStackSize(int index, int count) {
        return VersionUtil.empty();
    }

    @Override
    default ItemStack removeStackFromSlot(int index) {
        return VersionUtil.empty();
    }

    @Override
    default void setInventorySlotContents(int index, ItemStack stack) {
    }

    @Override
    default int getInventoryStackLimit() {
        return 0;
    }

    @Override
    default boolean isUsableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    default boolean isItemValidForSlot(int index, ItemStack stack) {
        return false;
    }

    @Override
    default void clear() {
    }
}
