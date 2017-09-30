/*
 * Copyright (C) 2012,2013 yogpstop This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.yogpc.qp.container;

import com.yogpc.qp.tile.TilePlacer;
import com.yogpc.qp.version.VersionUtil;
import javax.annotation.Nonnull;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerPlacer extends Container {
    private final TilePlacer tile;

    public ContainerPlacer(final IInventory inventory, final TilePlacer placer) {
        this.tile = placer;
        int row;
        int col;

        for (row = 0; row < 3; ++row)
            for (col = 0; col < 3; ++col)
                addSlotToContainer(new Slot(placer, col + row * 3, 62 + col * 18, 17 + row * 18));

        for (row = 0; row < 3; ++row)
            for (col = 0; col < 9; ++col)
                addSlotToContainer(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));

        for (col = 0; col < 9; ++col)
            addSlotToContainer(new Slot(inventory, col, 8 + col * 18, 142));
    }

    @Override
    public boolean canInteractWith(final EntityPlayer playerIn) {
        return this.tile.isUsableByPlayer(playerIn);
    }

    @Override
    @Nonnull
    public ItemStack transferStackInSlot(final EntityPlayer playerIn, final int index) {
        ItemStack src = VersionUtil.empty();
        final Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            final ItemStack remain = slot.getStack();
            src = remain.copy();
            if (index < 9) {
                if (!mergeItemStack(remain, 9, 45, true))
                    return VersionUtil.empty();
            } else if (!mergeItemStack(remain, 0, 9, false))
                return VersionUtil.empty();
            if (remain.getCount() == 0)
                slot.putStack(VersionUtil.empty());
            else
                slot.onSlotChanged();
            if (remain.getCount() == src.getCount())
                return VersionUtil.empty();
            VersionUtil.onTake(slot, playerIn, remain);
        }
        return src;
    }
}
