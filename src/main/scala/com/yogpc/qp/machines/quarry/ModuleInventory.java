package com.yogpc.qp.machines.quarry;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;

public class ModuleInventory extends InventoryBasic {
    private final TileEntity tile;

    public ModuleInventory(ITextComponent title, int slotCount, TileEntity entity) {
        super(title, slotCount);
        tile = entity;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        // TODO
        return super.isItemValidForSlot(index, stack);
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return player.getDistanceSq(tile.getPos()) <= 64;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }
}
