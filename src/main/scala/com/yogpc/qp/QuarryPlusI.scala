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
package com.yogpc.qp

import com.yogpc.qp.block._
import com.yogpc.qp.item.{ItemMirror, ItemQuarryDebug, ItemTool}
import com.yogpc.qp.tile.TileMarker
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object QuarryPlusI {
    val instance = this
    val creativeTab = new CreativeTabQuarryPlus
    val blockQuarry = new BlockQuarry
    val blockMarker = new BlockMarker
    val blockMover = new BlockMover
    val blockMiningWell = new BlockMiningWell
    val blockPump = new BlockPump
    val blockRefinery = new BlockRefinery
    val blockPlacer = new BlockPlacer
    val blockBreaker = new BlockBreaker
    val blockLaser = new BlockLaser
    val blockPlainPipe = new BlockPlainPipe
    val blockFrame = new BlockFrame
    val workbench = new BlockWorkbench
    val controller = new BlockController
    val blockChunkdestroyer = new BlockAdvQuarry
    val blockStandalonePump = new BlockAdvPump
    val itemTool = ItemTool.item
    val magicmirror = new ItemMirror
    val debugItem = ItemQuarryDebug.item
    val guiIdWorkbench = 1
    val guiIdMover = 2
    val guiIdFList = 3
    val guiIdSList = 4
    val guiIdPlacer = 5
    val guiIdAdvQuarry = 6
    val guiIdAdvPump = 7

    @SubscribeEvent
    def onWorldUnload(event: WorldEvent.Unload): Unit = {
        val la = TileMarker.linkList.toArray(new Array[TileMarker.Link](TileMarker.linkList.size))
        for (l <- la) {
            if (l.w eq event.getWorld) l.removeConnection(false)
        }
        val lb = TileMarker.laserList.toArray(new Array[TileMarker.Laser](TileMarker.laserList.size))
        for (l <- lb) {
            if (l.w eq event.getWorld) l.destructor()
        }
    }
}