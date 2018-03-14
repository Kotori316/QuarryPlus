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

import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.advquarry.AdvModeMessage;
import com.yogpc.qp.tile.TileAdvQuarry;
import com.yogpc.qp.version.VersionUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerAdvQuarry extends Container {
    private final TileAdvQuarry tile;

    public ContainerAdvQuarry(TileAdvQuarry tile, EntityPlayer player) {
        this.tile = tile;
        int oneBox = 18;
        for (int h = 0; h < 3; h++) {
            for (int v = 0; v < 9; v++) {
                this.addSlotToContainer(new Slot(player.inventory, v + h * 9 + 9, 8 + v * oneBox, 84 + h * oneBox));
            }
        }
        for (int vertical = 0; vertical < 9; vertical++) {
            this.addSlotToContainer(new Slot(player.inventory, vertical, 8 + vertical * oneBox, 142));
        }
        if (!tile.getWorld().isRemote) PacketHandler.sendToClient(AdvModeMessage.create(tile), (EntityPlayerMP) player);
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return tile.isUsableByPlayer(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        return VersionUtil.empty();
    }
}
