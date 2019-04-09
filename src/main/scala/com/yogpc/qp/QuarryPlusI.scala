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
import com.yogpc.qp.tile._
import net.minecraft.block.Block
import net.minecraft.util.ResourceLocation

import scala.collection.mutable.ListBuffer

object QuarryPlusI {
  private[this] val blocks = new ListBuffer[Block]
  val creativeTab = new CreativeTabQuarryPlus
  val blockQuarry: BlockQuarry = register(new BlockQuarry)
  val blockMarker: BlockMarker = register(new BlockMarker)
  val blockMover: BlockMover = register(new BlockMover)
  val blockMiningWell: BlockMiningWell = register(new BlockMiningWell)
  val blockPump: BlockPump = register(new BlockPump)
  val blockRefinery: BlockRefinery = register(new BlockRefinery)
  val blockPlacer: BlockPlacer = register(new BlockPlacer)
  val blockBreaker: BlockBreaker = register(new BlockBreaker)
  val blockLaser: BlockLaser = register(new BlockLaser)
  val blockPlainPipe: BlockPlainPipe = register(new BlockPlainPipe)
  val blockFrame: BlockFrame = register(new BlockFrame)
  val blockWorkbench: BlockWorkbench = register(new BlockWorkbench)
  val blockController: BlockController = register(new BlockController)
  val blockChunkDestroyer: BlockAdvQuarry = register(new BlockAdvQuarry)
  val blockStandalonePump: BlockAdvPump = register(new BlockAdvPump)
  val blockBookMover: BlockBookMover = register(new BlockBookMover)
  val blockExpPump: BlockExpPump = register(new BlockExpPump())
  val blockSolidQuarry: BlockSolidQuarry = register(new BlockSolidQuarry)
  val dummyBlock: DummyBlock = register(new DummyBlock)
  val blockReplacer: BlockReplacer = register(new BlockReplacer)
  val itemTool = new ItemTool
  val magicMirror = new ItemMirror
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
  final val guiIdQuarryYLevel = 10
  final val guiIdAdvQuarryYLevel = 11

  val tileIdMap = Map(
    classOf[TileWorkbench] -> QuarryPlus.Names.workbench,
    classOf[TileQuarry] -> QuarryPlus.Names.quarry,
    classOf[TileMarker] -> QuarryPlus.Names.marker,
    classOf[TileMiningWell] -> QuarryPlus.Names.miningwell,
    classOf[TilePump] -> QuarryPlus.Names.pump,
    classOf[TileRefinery] -> QuarryPlus.Names.refinery,
    classOf[TilePlacer] -> QuarryPlus.Names.placer,
    classOf[TileBreaker] -> QuarryPlus.Names.breaker,
    classOf[TileLaser] -> QuarryPlus.Names.laser,
    classOf[TileAdvQuarry] -> QuarryPlus.Names.advquarry,
    classOf[TileAdvPump] -> QuarryPlus.Names.advpump,
    classOf[TileBookMover] -> QuarryPlus.Names.moverfrombook,
    classOf[TileExpPump] -> QuarryPlus.Names.exppump,
    classOf[TileSolidQuarry] -> QuarryPlus.Names.solidquarry,
    classOf[TileReplacer] -> QuarryPlus.Names.replacer
  ).mapValues(s => new ResourceLocation(QuarryPlus.modID, s))

  val tileIdSet = tileIdMap.map { case (_, s) => s.toString }.toSet

  private def register[T <: Block](block: T): T = {
    blocks += block
    block
  }

  def blockList(): util.List[Block] = {
    import scala.collection.JavaConverters._
    new util.ArrayList[Block](blocks.asJava)
  }
}
