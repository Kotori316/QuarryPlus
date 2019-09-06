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
package com.yogpc.qp.machines.advquarry;

import java.util.Objects;

import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.advquarry.AdvModeMessage;
import com.yogpc.qp.utils.Holder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class ContainerAdvQuarry extends Container {
    public final TileAdvQuarry tile;

    public ContainerAdvQuarry(int id, PlayerEntity player, BlockPos pos) {
        super(Holder.advQuarryContainerType(), id);
        this.tile = (TileAdvQuarry) (Objects.requireNonNull(player.getEntityWorld().getTileEntity(pos)));
        int oneBox = 18;
        for (int h = 0; h < 3; h++) {
            for (int v = 0; v < 9; v++) {
                this.addSlot(new Slot(player.inventory, v + h * 9 + 9, 8 + v * oneBox, 84 + h * oneBox));
            }
        }
        for (int vertical = 0; vertical < 9; vertical++) {
            this.addSlot(new Slot(player.inventory, vertical, 8 + vertical * oneBox, 142));
        }
        if (!player.world.isRemote) PacketHandler.sendToClient(AdvModeMessage.create(tile), player.world);
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return tile.isUsableByPlayer(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        return ItemStack.EMPTY;
    }
}
