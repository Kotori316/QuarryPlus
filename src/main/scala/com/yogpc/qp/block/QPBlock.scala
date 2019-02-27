package com.yogpc.qp.block

import java.util.function.Function

import com.yogpc.qp.compat.InvUtils
import com.yogpc.qp.tile.IEnchantableTile
import com.yogpc.qp.{QuarryPlus, QuarryPlusI}
import net.minecraft.block.BlockContainer
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLiving
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{ItemBlock, ItemStack}
import net.minecraft.util.math.{BlockPos, RayTraceResult}
import net.minecraft.util.{EnumBlockRenderType, EnumFacing, EnumHand}
import net.minecraft.world.{IBlockAccess, World}

abstract class QPBlock(materialIn: Material, name: String, generator: QPBlock => _ <: ItemBlock, dummy: Boolean = false) extends BlockContainer(materialIn) {

  def this(materialIn: Material, name: String, generator: Function[QPBlock, _ <: ItemBlock]) = {
    this(materialIn, name, (block: QPBlock) => generator.apply(block))
  }

  setUnlocalizedName(name)
  setRegistryName(QuarryPlus.modID, name)
  setCreativeTab(QuarryPlusI.creativeTab)
  val itemBlock = generator.apply(this)
  itemBlock.setRegistryName(QuarryPlus.modID, name)

  override def getRenderType(state: IBlockState): EnumBlockRenderType = EnumBlockRenderType.MODEL

  override def canCreatureSpawn(state: IBlockState, world: IBlockAccess, pos: BlockPos, t: EntityLiving.SpawnPlacementType) = false

  override def onBlockActivated(worldIn: World, pos: BlockPos, state: IBlockState, playerIn: EntityPlayer,
                                hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean =
    InvUtils.isDebugItem(playerIn, hand) || super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ)

  override def rotateBlock(world: World, pos: BlockPos, axis: EnumFacing): Boolean = false

  override def getPickBlock(state: IBlockState, target: RayTraceResult, world: World, pos: BlockPos, player: EntityPlayer): ItemStack = {
    val tile = world.getTileEntity(pos)
    tile match {
      case enchantable: IEnchantableTile =>
        val stack = new ItemStack(this, 1, damageDropped(state))
        IEnchantableTile.Util.enchantmentToIS(enchantable, stack)
        stack
      case _ => super.getPickBlock(state, target, world, pos, player)
    }
  }

  override def getComparatorInputOverride(blockState: IBlockState, worldIn: World, pos: BlockPos): Int = {
    if (blockState.getValue(ADismCBlock.ACTING)) 15 else 0
  }

  override def hasComparatorInputOverride(state: IBlockState): Boolean = state.getPropertyKeys.contains(ADismCBlock.ACTING)
}
