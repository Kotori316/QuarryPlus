package com.yogpc.qp.tile;

import net.minecraft.entity.player.EntityPlayer;

public interface HasInv extends net.minecraft.inventory.IInventory {

    @Override
    default void openInventory(EntityPlayer player) {
    }

    @Override
    default void closeInventory(EntityPlayer player) {
    }

    @Override
    default int getField(int id) {
        return 0;
    }

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
}
