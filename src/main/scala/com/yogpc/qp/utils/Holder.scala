package com.yogpc.qp.utils

import com.yogpc.qp.machines.advpump.{BlockAdvPump, TileAdvPump}
import com.yogpc.qp.machines.advquarry.{BlockAdvQuarry, TileAdvQuarry}
import com.yogpc.qp.machines.base.IDisabled
import com.yogpc.qp.machines.bookmover.{BlockBookMover, TileBookMover}
import com.yogpc.qp.machines.controller.BlockController
import com.yogpc.qp.machines.exppump.{BlockExpPump, TileExpPump}
import com.yogpc.qp.machines.item._
import com.yogpc.qp.machines.marker.{BlockMarker, TileMarker}
import com.yogpc.qp.machines.modules.{ItemExpPumpModule, ItemPumpModule, ItemReplacerModule}
import com.yogpc.qp.machines.mover.BlockMover
import com.yogpc.qp.machines.pump.{BlockPump, TilePump}
import com.yogpc.qp.machines.quarry._
import com.yogpc.qp.machines.replacer.{BlockDummy, BlockReplacer, TileReplacer}
import com.yogpc.qp.machines.workbench.{BlockWorkbench, TileWorkbench}
import com.yogpc.qp.{CreativeTabQuarryPlus, QuarryPlus}
import net.minecraft.block.Block
import net.minecraft.item.Item
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
  val pumpTileType = createType(() => new TilePump, QuarryPlus.Names.pump)
  val solidQuarryType = createType(() => new TileSolidQuarry, QuarryPlus.Names.solidquarry)
  val replacerType = createType(() => new TileReplacer, QuarryPlus.Names.replacer)
  val bookMoverType = createType(() => new TileBookMover, QuarryPlus.Names.moverfrombook)
  val advQuarryType = createType(() => new TileAdvQuarry, QuarryPlus.Names.advquarry)
  val advPumpType = createType(() => new TileAdvPump, QuarryPlus.Names.advpump)
  val quarry2 = createType(() => new TileQuarry2, QuarryPlus.Names.quarry2)

  val tiles: Map[TileEntityType[_ <: TileEntity], TileDisable] = Map(
    markerTileType -> TileDisable(TileMarker.SYMBOL),
    workbenchTileType -> TileDisable(TileWorkbench.SYMBOL),
    expPumpTileType -> TileDisable(BlockExpPump.SYMBOL),
    miningWellTileType -> TileDisable(TileMiningWell.SYMBOL),
    quarryTileType -> TileDisable(TileQuarry.SYMBOL),
    pumpTileType -> TileDisable(TilePump.SYMBOL),
    solidQuarryType -> TileDisable(BlockSolidQuarry.SYMBOL),
    replacerType -> TileDisable(TileReplacer.SYMBOL, defaultDisableMachine = true),
    bookMoverType -> TileDisable(BlockBookMover.SYMBOL, defaultDisableMachine = true),
    advQuarryType -> TileDisable(TileAdvQuarry.SYMBOL, defaultDisableMachine = true),
    advPumpType -> TileDisable(TileAdvPump.SYMBOL),
    quarry2 -> TileDisable(TileQuarry2.SYMBOL),
  )

  //---------- Block ----------

  val blockMarker = new BlockMarker
  val blockWorkbench = new BlockWorkbench
  val blockExpPump = new BlockExpPump
  val blockMover = new BlockMover
  val blockMiningWell = new BlockMiningWell
  val blockPlainPipe = new BlockPlainPipe
  val blockFrame = new BlockFrame
  val blockQuarry = new BlockQuarry
  val blockPump = new BlockPump
  val blockSolidQuarry = new BlockSolidQuarry
  val blockDummy = new BlockDummy
  val blockReplacer = new BlockReplacer
  val blockController = new BlockController
  val blockBookMover = new BlockBookMover
  val blockAdvQuarry = new BlockAdvQuarry
  val blockAdvPump = new BlockAdvPump
  val blockQuarry2 = new BlockQuarry2

  val blocks: Seq[Block] = Seq(
    blockQuarry,
    blockMiningWell,
    blockPump,
    blockSolidQuarry,
    blockMarker,
    blockWorkbench,
    blockMover,
    blockExpPump,
    blockPlainPipe,
    blockFrame,
    blockDummy,
    blockReplacer,
    blockController,
    blockBookMover,
    blockAdvQuarry,
    blockAdvPump,
    blockQuarry2,
  )
  //---------- Item ----------

  val itemListEditor = new ItemListEditor
  val itemLiquidSelector = new ItemLiquidSelector
  val itemStatusChecker = new Item((new Item.Properties).group(Holder.tab)).setRegistryName(QuarryPlus.modID, QuarryPlus.Names.statuschecker)
  val itemYSetter = new Item((new Item.Properties).group(Holder.tab)).setRegistryName(QuarryPlus.modID, QuarryPlus.Names.ySetter)
  val itemQuarryDebug = new ItemQuarryDebug
  val itemTemplate = new ItemTemplate
  val itemPumpModule = new ItemPumpModule
  val itemExpPumpModule = new ItemExpPumpModule
  val itemReplacerModule = new ItemReplacerModule

  val items: Seq[Item] = Seq(itemStatusChecker, itemListEditor, itemLiquidSelector, itemYSetter, itemQuarryDebug, itemTemplate,
    itemPumpModule, itemExpPumpModule, itemReplacerModule)

  //---------- IDisable ----------
  case class TileDisable(override val getSymbol: Symbol, override val defaultDisableMachine: Boolean = false) extends IDisabled

  val canDisablesSymbols = (tiles.values ++ blocks ++ items).collect { case d: IDisabled => d.getSymbol -> d.defaultDisableMachine }
}
