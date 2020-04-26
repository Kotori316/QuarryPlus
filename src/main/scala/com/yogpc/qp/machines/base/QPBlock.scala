package com.yogpc.qp.machines.base

import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.compat.InvUtils
import com.yogpc.qp.machines.TranslationKeys
import com.yogpc.qp.utils.Holder
import net.minecraft.block.{Block, BlockRenderType, BlockState, ContainerBlock}
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.{EntitySpawnPlacementRegistry, EntityType}
import net.minecraft.inventory.InventoryHelper
import net.minecraft.item.{BlockItem, Item, ItemStack}
import net.minecraft.state.BooleanProperty
import net.minecraft.tileentity.{TileEntity, TileEntityType}
import net.minecraft.util.math.{BlockPos, BlockRayTraceResult, RayTraceResult}
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.util.{ActionResultType, Hand, NonNullList, ResourceLocation}
import net.minecraft.world.server.ServerWorld
import net.minecraft.world.{IBlockReader, World}

abstract class QPBlock(builder: Block.Properties, name: String, generator: java.util.function.BiFunction[QPBlock, Item.Properties, _ <: BlockItem]) extends ContainerBlock(builder) {

  setRegistryName(QuarryPlus.modID, name)
  val BlockItem: BlockItem = generator.apply(this, new Item.Properties().group(Holder.tab))
  BlockItem.setRegistryName(QuarryPlus.modID, name)

  override def asItem(): Item = BlockItem

  //noinspection ScalaDeprecation
  override def getRenderType(state: BlockState): BlockRenderType = BlockRenderType.MODEL

  override def canCreatureSpawn(state: BlockState, world: IBlockReader, pos: BlockPos, t: EntitySpawnPlacementRegistry.PlacementType, entityType: EntityType[_]): Boolean = {
    false
  }

  override def getPickBlock(state: BlockState, target: RayTraceResult, world: IBlockReader, pos: BlockPos, player: PlayerEntity): ItemStack = {
    val tile = world.getTileEntity(pos)
    tile match {
      case enchantable: IEnchantableTile =>
        val stack = new ItemStack(BlockItem, 1)
        IEnchantableTile.Util.enchantmentToIS(enchantable, stack)
        stack
      case _ => super.getPickBlock(state, target, world, pos, player)
    }
  }

  //noinspection ScalaDeprecation
  override def onBlockActivated(state: BlockState, worldIn: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockRayTraceResult): ActionResultType = {
    if (Holder.tiles.get(getTileType).fold(false)(!_.enabled)) {
      if (worldIn.isRemote)
        player.sendStatusMessage(new TranslationTextComponent(TranslationKeys.DISABLE_MESSAGE, getNameTextComponent), true)
      ActionResultType.SUCCESS // Skip other operations because this tile is DISABLED.
    } else {
      InvUtils.isDebugItem(player, hand) // super method return false.
    }
  }

  //noinspection ScalaDeprecation
  override def getComparatorInputOverride(blockState: BlockState, worldIn: World, pos: BlockPos): Int = {
    if (blockState.get(QPBlock.WORKING)) 15 else 0
  }

  //noinspection ScalaDeprecation
  override def hasComparatorInputOverride(state: BlockState): Boolean = state.getProperties.contains(QPBlock.WORKING)

  def getTileType: TileEntityType[_ <: TileEntity]

  override final def createNewTileEntity(worldIn: IBlockReader): TileEntity = getTileType.create()
}

object QPBlock {
  val WORKING: BooleanProperty = BooleanProperty.create("working")
  val contentLocation = new ResourceLocation(QuarryPlus.modID, "content")

  def dismantle(world: World, pos: BlockPos, state: BlockState, returnDrops: Boolean): NonNullList[ItemStack] = {
    val list = NonNullList.create[ItemStack]
    Block.getDrops(state, world.asInstanceOf[ServerWorld], pos, world.getTileEntity(pos))
    world.removeBlock(pos, false)
    if (!returnDrops) {
      import scala.jdk.CollectionConverters._
      for (drop <- list.asScala) {
        InventoryHelper.spawnItemStack(world, pos.getX, pos.getY, pos.getZ, drop)
      }
    }
    list
  }

}
