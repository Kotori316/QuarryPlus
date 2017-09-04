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

package com.yogpc.qp;

import java.util.List;

import com.google.common.collect.Lists;
import com.yogpc.qp.tile.TileMarker;
import com.yogpc.qp.tile.TileQuarry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

public class ChunkLoadingHandler implements ForgeChunkManager.OrderedLoadingCallback {
    @Override
    public void ticketsLoaded(final List<Ticket> tickets, final World world) {
        for (final Ticket ticket : tickets) {
            final int quarryX = ticket.getModData().getInteger("quarryX");
            final int quarryY = ticket.getModData().getInteger("quarryY");
            final int quarryZ = ticket.getModData().getInteger("quarryZ");
            final TileEntity te = world.getTileEntity(new BlockPos(quarryX, quarryY, quarryZ));
            if (te instanceof TileQuarry)
                ((TileQuarry) te).forceChunkLoading(ticket);
            else if (te instanceof TileMarker)
                ((TileMarker) te).forceChunkLoading(ticket);
        }
    }

    @Override
    public List<Ticket> ticketsLoaded(final List<Ticket> tickets, final World world,
                                      final int maxTicketCount) {
        final List<Ticket> validTickets = Lists.newArrayList();
        for (final Ticket ticket : tickets) {
            final int quarryX = ticket.getModData().getInteger("quarryX");
            final int quarryY = ticket.getModData().getInteger("quarryY");
            final int quarryZ = ticket.getModData().getInteger("quarryZ");

            IBlockState state = world.getBlockState(new BlockPos(quarryX, quarryY, quarryZ));
            if (state.getBlock() == QuarryPlusI.blockQuarry || state.getBlock() == QuarryPlusI.blockMarker)
                validTickets.add(ticket);
        }
        return validTickets;
    }

}
