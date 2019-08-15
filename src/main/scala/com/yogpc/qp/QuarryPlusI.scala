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
import com.yogpc.qp.item.{ItemMirror, ItemQuarryDebug, ItemTemplate, ItemTool}
import com.yogpc.qp.modules._
import com.yogpc.qp.tile._
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.util.ResourceLocation

import scala.collection.mutable.ListBuffer

object QuarryPlusI {
  private[this] val blocks = new ListBuffer[Block]
  private[this] val items = new ListBuffer[Item]
  val creativeTab = new CreativeTabQuarryPlus
  val blockQuarry: BlockQuarry = registerB(new BlockQuarry)
  val blockMarker: BlockMarker = registerB(new BlockMarker)
  val blockMover: BlockMover = registerB(new BlockMover)
  val blockMiningWell: BlockMiningWell = registerB(new BlockMiningWell)
  val blockPump: BlockPump = registerB(new BlockPump)
  val blockRefinery: BlockRefinery = registerB(new BlockRefinery)
  val blockPlacer: BlockPlacer = registerB(new BlockPlacer)
  val blockBreaker: BlockBreaker = registerB(new BlockBreaker)
  val blockLaser: BlockLaser = registerB(new BlockLaser)
  val blockPlainPipe: BlockPlainPipe = registerB(new BlockPlainPipe)
  val blockFrame: BlockFrame = registerB(new BlockFrame)
  val blockWorkbench: BlockWorkbench = registerB(new BlockWorkbench)
  val blockController: BlockController = registerB(new BlockController)
  val blockChunkDestroyer: BlockAdvQuarry = registerB(new BlockAdvQuarry)
  val blockStandalonePump: BlockAdvPump = registerB(new BlockAdvPump)
  val blockBookMover: BlockBookMover = registerB(new BlockBookMover)
  val blockExpPump: BlockExpPump = registerB(new BlockExpPump())
  val blockSolidQuarry: BlockSolidQuarry = registerB(new BlockSolidQuarry)
  val dummyBlock: DummyBlock = registerB(new DummyBlock)
  val blockReplacer: BlockReplacer = registerB(new BlockReplacer)
  val blockQuarry2: BlockQuarry2 = registerB(new BlockQuarry2)
  val itemTool = registerI(new ItemTool)
  val magicMirror = registerI(new ItemMirror)
  val debugItem = registerI(new ItemQuarryDebug)
  val itemTemplate = registerI(new ItemTemplate)
  val pumpModule = registerI(new ItemPumpModule)
  val expPumpModule = registerI(new ItemExpPumpModule)
  val replacerModule = registerI(new ItemReplacerModule)
  val torchModule = registerI(new ItemTorchModule)
  val fuelModuleNormal = registerI(new ItemFuelModule(FuelModule.Normal))
  val fuelModuleCreative = registerI(new ItemFuelModule(FuelModule.Creative))
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
  final val guiIdListTemplate = 12
  final val guiIdQuarry2 = 13
  final val guiIdQuarry2YLevel = 14

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
    classOf[TileReplacer] -> QuarryPlus.Names.replacer,
    classOf[TileQuarry2] -> QuarryPlus.Names.quarry2
  ).mapValues(s => new ResourceLocation(QuarryPlus.modID, s))

  val tileIdSet = tileIdMap.map { case (_, s) => s.toString }.toSet

  private def registerB[T <: Block](block: T): T = {
    blocks += block
    block
  }

  private def registerI[T <: Item](block: T): T = {
    items += block
    block
  }

  def blockList(): util.List[Block] = {
    import scala.collection.JavaConverters._
    new util.ArrayList[Block](blocks.asJava)
  }

  def itemList(): util.List[Item] = {
    import scala.collection.JavaConverters._
    new util.ArrayList[Item](items.asJava)
  }

  def itemDisableInfo: Set[IDisabled] = {
    items.collect { case i: IDisabled => i }.toSet
  }
}
