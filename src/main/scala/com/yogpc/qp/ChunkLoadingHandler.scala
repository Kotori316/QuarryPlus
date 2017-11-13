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
 *//*
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
package com.yogpc.qp

import java.util

import com.yogpc.qp.tile.{TileAdvQuarry, TileMarker, TileQuarry}
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.ForgeChunkManager

import scala.collection.JavaConverters._

object ChunkLoadingHandler extends ForgeChunkManager.OrderedLoadingCallback {
    val instance = this

    override def ticketsLoaded(tickets: util.List[ForgeChunkManager.Ticket], world: World): Unit = {
        for (ticket <- tickets.asScala) {
            val quarryX = ticket.getModData.getInteger("quarryX")
            val quarryY = ticket.getModData.getInteger("quarryY")
            val quarryZ = ticket.getModData.getInteger("quarryZ")
            val te = world.getTileEntity(new BlockPos(quarryX, quarryY, quarryZ))
            te match {
                case quarry: TileQuarry => quarry.forceChunkLoading(ticket)
                case marker: TileMarker => marker.forceChunkLoading(ticket)
                case chunkQuaryy: TileAdvQuarry => chunkQuaryy.forceChunkLoading(ticket)
                case _ =>
            }
        }
    }

    override def ticketsLoaded(tickets: util.List[ForgeChunkManager.Ticket], world: World, maxTicketCount: Int): util.List[ForgeChunkManager.Ticket] = {
        val validTickets = new util.ArrayList[ForgeChunkManager.Ticket]()
        for (ticket <- tickets.asScala) {
            val quarryX = ticket.getModData.getInteger("quarryX")
            val quarryY = ticket.getModData.getInteger("quarryY")
            val quarryZ = ticket.getModData.getInteger("quarryZ")
            val state = world.getBlockState(new BlockPos(quarryX, quarryY, quarryZ))
            if (state.getBlock == QuarryPlusI.blockQuarry || state.getBlock == QuarryPlusI.blockMarker
              || state.getBlock == QuarryPlusI.blockChunkdestroyer)
                validTickets.add(ticket)
        }
        validTickets
    }
}