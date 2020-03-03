package com.yogpc.qp.utils

import com.mojang.datafixers.DSL
import com.yogpc.qp.machines.advpump.{BlockAdvPump, ContainerAdvPump, TileAdvPump}
import com.yogpc.qp.machines.advquarry.{BlockAdvQuarry, ContainerAdvQuarry, TileAdvQuarry}
import com.yogpc.qp.machines.base.{IDisabled, StatusContainer}
import com.yogpc.qp.machines.bookmover.{BlockBookMover, ContainerBookMover, TileBookMover}
import com.yogpc.qp.machines.controller.BlockController
import com.yogpc.qp.machines.exppump.{BlockExpPump, TileExpPump}
import com.yogpc.qp.machines.item._
import com.yogpc.qp.machines.marker.{BlockMarker, TileMarker}
import com.yogpc.qp.machines.modules._
import com.yogpc.qp.machines.mover.{BlockMover, ContainerMover}
import com.yogpc.qp.machines.pb.{PlacerBlock, PlacerContainer, PlacerTile}
import com.yogpc.qp.machines.pump.{BlockPump, TilePump}
import com.yogpc.qp.machines.quarry._
import com.yogpc.qp.machines.replacer.{BlockDummy, BlockReplacer, TileReplacer}
import com.yogpc.qp.machines.workbench.{BlockWorkbench, ContainerWorkbench, TileWorkbench}
import com.yogpc.qp.{CreativeTabQuarryPlus, QuarryPlus}
import net.minecraft.block.Block
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.container.{Container, ContainerType}
import net.minecraft.item.Item
import net.minecraft.tileentity.{TileEntity, TileEntityType}
import net.minecraft.util.math.BlockPos
import net.minecraftforge.common.extensions.IForgeContainerType
import net.minecraftforge.fml.ModLoadingContext

object Holder {
  if (ModLoadingContext.get.getActiveContainer.getModId != QuarryPlus.modID) {
    QuarryPlus.LOGGER.error(s"Called in loading ${ModLoadingContext.get.getActiveContainer.getModId}.")
  }
  //---------- Item Group ----------
  val tab = new CreativeTabQuarryPlus

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
  val blockPlacer = new PlacerBlock

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
    blockPlacer,
  )

  //---------- TileEntity ----------
  private def createTileType[T <: TileEntity](supplier: () => T, name: String, block: Block): TileEntityType[T] = {
    val t = TileEntityType.Builder.create[T](() => supplier(), block).build(DSL.nilType())
    t.setRegistryName(QuarryPlus.modID, name)
    t
  }

  val markerTileType = createTileType(() => new TileMarker, QuarryPlus.Names.marker, blockMarker)
  val workbenchTileType = createTileType(() => new TileWorkbench, QuarryPlus.Names.workbench, blockWorkbench)
  val expPumpTileType = createTileType(() => new TileExpPump, QuarryPlus.Names.exppump, blockExpPump)
  val miningWellTileType = createTileType(() => new TileMiningWell, QuarryPlus.Names.miningwell, blockMiningWell)
  val quarryTileType = createTileType(() => new TileQuarry, QuarryPlus.Names.quarry, blockQuarry)
  val pumpTileType = createTileType(() => new TilePump, QuarryPlus.Names.pump, blockPump)
  val solidQuarryType = createTileType(() => new TileSolidQuarry, QuarryPlus.Names.solidquarry, blockSolidQuarry)
  val replacerType = createTileType(() => new TileReplacer, QuarryPlus.Names.replacer, blockReplacer)
  val bookMoverType = createTileType(() => new TileBookMover, QuarryPlus.Names.moverfrombook, blockBookMover)
  val advQuarryType = createTileType(() => new TileAdvQuarry, QuarryPlus.Names.advquarry, blockAdvQuarry)
  val advPumpType = createTileType(() => new TileAdvPump, QuarryPlus.Names.advpump, blockAdvPump)
  val quarry2 = createTileType(() => new TileQuarry2, QuarryPlus.Names.quarry2, blockQuarry2)
  val placerType = createTileType(() => new PlacerTile, QuarryPlus.Names.placer, blockPlacer)

  val tiles: Map[TileEntityType[_ <: TileEntity], TileDisable] = Map(
    markerTileType -> TileDisable(TileMarker.SYMBOL),
    workbenchTileType -> TileDisable(TileWorkbench.SYMBOL),
    expPumpTileType -> TileDisable(BlockExpPump.SYMBOL),
    miningWellTileType -> TileDisable(TileMiningWell.SYMBOL),
    quarryTileType -> TileDisable(TileQuarry.SYMBOL, defaultDisableMachine = true),
    pumpTileType -> TileDisable(TilePump.SYMBOL),
    solidQuarryType -> TileDisable(BlockSolidQuarry.SYMBOL),
    replacerType -> TileDisable(TileReplacer.SYMBOL, defaultDisableMachine = true),
    bookMoverType -> TileDisable(BlockBookMover.SYMBOL, defaultDisableMachine = true),
    advQuarryType -> TileDisable(TileAdvQuarry.SYMBOL, defaultDisableMachine = true),
    advPumpType -> TileDisable(TileAdvPump.SYMBOL),
    quarry2 -> TileDisable(TileQuarry2.SYMBOL),
    placerType -> TileDisable(PlacerTile.SYMBOL),
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
  val itemTorchModule = new ItemTorchModule
  val itemFuelModuleNormal = new ItemFuelModule(FuelModule.Normal)
  val itemFuelModuleCreative = new ItemFuelModule(FuelModule.Creative)
  val itemRemoveBedrockModule = new ItemBedrockModule

  val items: Seq[Item] = Seq(
    itemStatusChecker,
    itemListEditor,
    itemLiquidSelector,
    itemYSetter,
    itemQuarryDebug,
    itemTemplate,
    itemPumpModule,
    itemExpPumpModule,
    itemReplacerModule,
    itemTorchModule,
    itemFuelModuleNormal,
    itemFuelModuleCreative,
    itemRemoveBedrockModule,
  )

  //---------- IDisable ----------
  case class TileDisable(override val getSymbol: Symbol, override val defaultDisableMachine: Boolean = false) extends IDisabled

  val canDisablesSymbols = (tiles.values ++ blocks ++ items).collect { case d: IDisabled => d.getSymbol -> d.defaultDisableMachine }

  //---------- Container ----------
  val moverContainerType = createContainerType((windowId, p, pos) => new ContainerMover(windowId, p, pos), BlockMover.GUI_ID)
  val workbenchContainerType = createContainerType(new ContainerWorkbench(_, _, _), TileWorkbench.GUI_ID)
  val bookMoverContainerType = createContainerType(new ContainerBookMover(_, _, _), BlockBookMover.GUI_ID)
  val ySetterContainerType = createContainerType(new ContainerQuarryLevel(_, _, _), YSetterInteractionObject.GUI_ID)
  val solidQuarryContainerType = createContainerType(new ContainerSolidQuarry(_, _, _), TileSolidQuarry.GUI_ID)
  val quarryModuleContainerType = createContainerType(new ContainerQuarryModule(_, _, _), ContainerQuarryModule.GUI_ID)
  val enchListContainerType = {
    val value = IForgeContainerType.create[ContainerEnchList]((id, inv, data) =>
      new ContainerEnchList(id, inv.player, data.readBlockPos(), data.readResourceLocation()))
    value.setRegistryName(ItemListEditor.GUI_ID)
    value
  }
  val templateContainerType = createContainerType(new ContainerListTemplate(_, _, _), ItemTemplate.GUI_ID)
  val advPumpContainerType = createContainerType(new ContainerAdvPump(_, _, _), TileAdvPump.GUI_ID)
  val advQuarryContainerType = createContainerType(new ContainerAdvQuarry(_, _, _), TileAdvQuarry.GUI_ID)
  val statusContainerType = createContainerType(new StatusContainer(_, _, _), StatusContainer.GUI_ID)
  val placerContainerType = createContainerType(new PlacerContainer(_, _, _), PlacerContainer.GUI_ID)

  val containers: Set[ContainerType[_ <: Container]] = Set(
    quarryModuleContainerType,
    solidQuarryContainerType,
    workbenchContainerType,
    moverContainerType,
    bookMoverContainerType,
    ySetterContainerType,
    enchListContainerType,
    templateContainerType,
    advPumpContainerType,
    advQuarryContainerType,
    statusContainerType,
    placerContainerType,
  )

  private def createContainerType[T <: Container](supplier: (Int, PlayerEntity, BlockPos) => T, name: String): ContainerType[T] = {
    val value = IForgeContainerType.create[T]((windowId, inv, data) => supplier(windowId, inv.player, data.readBlockPos()))
    value.setRegistryName(name)
    value
  }
}
