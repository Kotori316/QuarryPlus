package com.yogpc.qp.block

import java.util.function.Function

import com.yogpc.qp.compat.InvUtils
import com.yogpc.qp.{QuarryPlus, QuarryPlusI}
import net.minecraft.block.BlockContainer
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLiving
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemBlock
import net.minecraft.util.math.BlockPos
import net.minecraft.util.{EnumBlockRenderType, EnumFacing, EnumHand}
import net.minecraft.world.{IBlockAccess, World}
import net.minecraftforge.fml.common.Loader

abstract class QPBlock(materialIn: Material, name: String, generator: Function[QPBlock, _ <: ItemBlock]) extends BlockContainer(materialIn) {
    setUnlocalizedName(name)
    setRegistryName(QuarryPlus.modID, name)
    setCreativeTab(QuarryPlusI.creativeTab)
    val itemBlock = generator.apply(this)
    itemBlock.setRegistryName(QuarryPlus.modID, name)

    val bcLoaded: Boolean = Loader.isModLoaded(QuarryPlus.Optionals.Buildcraft_modID)

    override def getRenderType(state: IBlockState): EnumBlockRenderType = EnumBlockRenderType.MODEL

    override def canCreatureSpawn(state: IBlockState, world: IBlockAccess, pos: BlockPos, spawntype: EntityLiving.SpawnPlacementType) = false

    override def onBlockActivated(worldIn: World, pos: BlockPos, state: IBlockState, playerIn: EntityPlayer,
                                  hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean =
        InvUtils.isDebugItem(playerIn, hand) || super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ)

    override def rotateBlock(world: World, pos: BlockPos, axis: EnumFacing): Boolean = false
}