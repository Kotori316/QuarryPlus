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

import java.util

import com.yogpc.qp.block._
import com.yogpc.qp.item.{ItemMirror, ItemQuarryDebug, ItemTool}
import net.minecraft.block.Block

import scala.collection.mutable.ListBuffer

object QuarryPlusI {
    private[this] val blocks = new ListBuffer[Block]
    val creativeTab = new CreativeTabQuarryPlus
    val blockQuarry = register(new BlockQuarry)
    val blockMarker = register(new BlockMarker)
    val blockMover = register(new BlockMover)
    val blockMiningWell = register(new BlockMiningWell)
    val blockPump = register(new BlockPump)
    val blockRefinery = register(new BlockRefinery)
    val blockPlacer = register(new BlockPlacer)
    val blockBreaker = register(new BlockBreaker)
    val blockLaser = register(new BlockLaser)
    val blockPlainPipe = register(new BlockPlainPipe)
    val blockFrame = register(new BlockFrame)
    val blockWorkbench = register(new BlockWorkbench)
    val blockController = register(new BlockController)
    val blockChunkdestroyer = register(new BlockAdvQuarry)
    val blockStandalonePump = register(new BlockAdvPump)
    val blockBookMover = register(new BlockBookMover)
    val blockSolidQuarry = register(new BlockSolidQuarry)
    val itemTool = new ItemTool
    val magicmirror = new ItemMirror
    val debugItem = new ItemQuarryDebug
    final val guiIdWorkbench = 1
    final val guiIdMover = 2
    final val guiIdFList = 3
    final val guiIdSList = 4
    final val guiIdPlacer = 5
    final val guiIdAdvQuarry = 6
    final val guiIdAdvPump = 7
    final val guiIdMoverFromBook = 8
    final val guiIdSolidQuarry = 9

    private def register[T <: Block](block: T): T = {
        blocks += block
        block
    }

    def blockList(): util.List[Block] = {
        import scala.collection.JavaConverters._
        new util.ArrayList[Block](blocks.asJava)
    }
}
