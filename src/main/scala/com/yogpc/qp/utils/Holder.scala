package com.yogpc.qp.utils

import com.yogpc.qp.machines.exppump.{BlockExpPump, TileExpPump}
import com.yogpc.qp.machines.marker.{BlockMarker, TileMarker}
import com.yogpc.qp.machines.mover.BlockMover
import com.yogpc.qp.machines.quarry._
import com.yogpc.qp.machines.workbench.{BlockWorkbench, TileWorkbench}
import com.yogpc.qp.{CreativeTabQuarryPlus, QuarryPlus}
import net.minecraft.block.Block
import net.minecraft.tileentity.{TileEntity, TileEntityType}
import net.minecraftforge.fml.ModLoadingContext

object Holder {
  if (ModLoadingContext.get.getActiveContainer.getModId != QuarryPlus.modID) {
    QuarryPlus.LOGGER.error(s"Called in loading ${ModLoadingContext.get.getActiveContainer.getModId}.")
  }
  //---------- Item Group ----------
  val tab = new CreativeTabQuarryPlus

  //---------- TileEntity ----------
  private def createType[T <: TileEntity](supplier: () => T, name: String): TileEntityType[T] = {
    val t = TileEntityType.Builder.create[T](() => supplier()).build(null)
    t.setRegistryName(QuarryPlus.modID, name)
    t
  }

  val markerTileType = createType(() => new TileMarker, QuarryPlus.Names.marker)
  val workbenchTileType = createType(() => new TileWorkbench, QuarryPlus.Names.workbench)
  val expPumpTileType = createType(() => new TileExpPump, QuarryPlus.Names.exppump)
  val miningWellTileType = createType(() => new TileMiningWell, QuarryPlus.Names.miningwell)
  val quarryTileType = createType(() => new TileQuarry, QuarryPlus.Names.quarry)

  val tiles: Seq[TileEntityType[_ <: TileEntity]] = Seq(markerTileType, workbenchTileType, expPumpTileType, miningWellTileType, quarryTileType)

  //---------- Block ----------

  val blockMarker = new BlockMarker
  val blockWorkbench = new BlockWorkbench
  val blockExpPump = new BlockExpPump
  val blockMover = new BlockMover
  val blockMiningWell = new BlockMiningWell
  val blockPlainPipe = new BlockPlainPipe
  val blockFrame = new BlockFrame
  val blockQuarry = new BlockQuarry

  val blocks: Seq[Block] = Seq(
    blockQuarry,
    blockMiningWell,
    blockMarker,
    blockWorkbench,
    blockMover,
    blockExpPump,
    blockPlainPipe,
    blockFrame,
  )
  //---------- Item ----------
}
