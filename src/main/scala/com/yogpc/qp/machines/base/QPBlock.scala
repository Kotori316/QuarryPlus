package com.yogpc.qp.machines.base

import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.compat.InvUtils
import com.yogpc.qp.utils.Holder
import net.minecraft.block.state.IBlockState
import net.minecraft.block.{Block, BlockContainer}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.{EntityLiving, EntitySpawnPlacementRegistry, EntityType}
import net.minecraft.item.{Item, ItemBlock, ItemStack}
import net.minecraft.state.BooleanProperty
import net.minecraft.util.math.{BlockPos, RayTraceResult}
import net.minecraft.util.{EnumBlockRenderType, EnumFacing, EnumHand}
import net.minecraft.world.{IBlockReader, IWorldReaderBase, World}

abstract class QPBlock(builder: Block.Properties, name: String, generator: java.util.function.BiFunction[QPBlock, Item.Properties, _ <: ItemBlock]) extends BlockContainer(builder) {

  setRegistryName(QuarryPlus.modID, name)
  val itemBlock = generator.apply(this, new Item.Properties().group(Holder.tab))
  itemBlock.setRegistryName(QuarryPlus.modID, name)

  override def asItem() = itemBlock

  override def getRenderType(state: IBlockState): EnumBlockRenderType = EnumBlockRenderType.MODEL

  override def canCreatureSpawn(state: IBlockState, world: IWorldReaderBase, pos: BlockPos,
                                t: EntitySpawnPlacementRegistry.SpawnPlacementType, entityType: EntityType[_ <: EntityLiving]) = {
    false
  }

  override def getPickBlock(state: IBlockState, target: RayTraceResult, world: IBlockReader, pos: BlockPos, player: EntityPlayer) = {
    val tile = world.getTileEntity(pos)
    tile match {
      case enchantable: IEnchantableTile =>
        val stack = new ItemStack(itemBlock, 1)
        IEnchantableTile.Util.enchantmentToIS(enchantable, stack)
        stack
      case _ => super.getPickBlock(state, target, world, pos, player)
    }
  }

  override def onBlockActivated(state: IBlockState, worldIn: World, pos: BlockPos, player: EntityPlayer,
                                hand: EnumHand, side: EnumFacing, hitX: Float, hitY: Float, hitZ: Float) = {
    InvUtils.isDebugItem(player, hand) // super method return false.
  }

  override def getComparatorInputOverride(blockState: IBlockState, worldIn: World, pos: BlockPos): Int = {
    if (blockState.get(QPBlock.WORKING)) 15 else 0
  }

  override def hasComparatorInputOverride(state: IBlockState): Boolean = state.getProperties.contains(QPBlock.WORKING)
}

object QPBlock {
  val WORKING = BooleanProperty.create("working")
}
