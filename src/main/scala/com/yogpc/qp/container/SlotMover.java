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

import com.yogpc.qp.item.IEnchantableItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagList;

public class SlotMover extends Slot {
    public SlotMover(final IInventory par1iInventory, int index, int xPosition, int yPosition) {
        super(par1iInventory, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(final ItemStack is) {
        switch (this.slotNumber) {
            case 0:
                final NBTTagList nbttl = is.getEnchantmentTagList();
                return nbttl != null
                        && (is.getItem() instanceof ItemTool
                        && ((ItemTool) is.getItem()).getToolMaterial() == Item.ToolMaterial.DIAMOND || is.getItem() instanceof ItemBow);
            case 1:
                if (is.getItem() instanceof IEnchantableItem)
                    return true;
        }
        return false;
    }

    @Override
    public int getSlotStackLimit() {
        return 1;
    }
}
