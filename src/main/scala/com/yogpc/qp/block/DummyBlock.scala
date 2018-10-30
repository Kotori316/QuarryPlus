package com.yogpc.qp.block

import com.yogpc.qp.{QuarryPlus, QuarryPlusI}
import net.minecraft.block.BlockEmptyDrops
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.{Entity, EntityLiving}
import net.minecraft.item.ItemBlock
import net.minecraft.util.math.BlockPos
import net.minecraft.util.{BlockRenderLayer, EnumFacing}
import net.minecraft.world.{IBlockAccess, World}
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

class DummyBlock extends BlockEmptyDrops(Material.GLASS) {
    setRegistryName(QuarryPlus.modID, QuarryPlus.Names.dummyblock)
    setUnlocalizedName(QuarryPlus.modID + "." + QuarryPlus.Names.dummyblock)
    setCreativeTab(QuarryPlusI.creativeTab)
    setHardness(1.0f)
    setLightOpacity(0)
    setLightLevel(1f)
    disableStats()

    val itemBlock = new ItemBlock(this)
    itemBlock.setRegistryName(QuarryPlus.modID, QuarryPlus.Names.dummyblock)

    override def canCreatureSpawn(state: IBlockState, world: IBlockAccess, pos: BlockPos, t: EntityLiving.SpawnPlacementType): Boolean = false

    override def canEntitySpawn(state: IBlockState, entityIn: Entity): Boolean = false

    override def canSilkHarvest(world: World, pos: BlockPos, state: IBlockState, player: EntityPlayer): Boolean = false

    @SideOnly(Side.CLIENT)
    override def getBlockLayer = BlockRenderLayer.CUTOUT

    override def isFullCube(state: IBlockState) = false

    override def isOpaqueCube(state: IBlockState) = false

    //noinspection ScalaDeprecation
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("deprecation")
    override def shouldSideBeRendered(blockState: IBlockState, blockAccess: IBlockAccess, pos: BlockPos, side: EnumFacing): Boolean = {
        val offsetState = blockAccess.getBlockState(pos offset side)
        offsetState.getBlock != this && super.shouldSideBeRendered(blockState, blockAccess, pos, side)
    }

}
