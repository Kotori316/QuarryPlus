package com.yogpc.qp.block;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.QuarryPlusI;
import net.minecraft.block.BlockEmptyDrops;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DummyBlock extends BlockEmptyDrops {
    private final ItemBlock mItemBlock;

    public DummyBlock() {
        super(Material.GLASS);
        setRegistryName(QuarryPlus.modID, QuarryPlus.Names.dummyblock);
        setUnlocalizedName(QuarryPlus.modID + "." + QuarryPlus.Names.dummyblock);
        setCreativeTab(QuarryPlusI.creativeTab());
        setHardness(1.0f);
        setLightOpacity(0);
        setLightLevel(1f);
        disableStats();
        mItemBlock = new ItemBlock(this);
        mItemBlock.setRegistryName(QuarryPlus.modID, QuarryPlus.Names.dummyblock);
    }

    public ItemBlock itemBlock() {
        return mItemBlock;
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean canEntitySpawn(IBlockState state, Entity entityIn) {
        return false;
    }

    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    @SuppressWarnings("deprecation")
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        IBlockState offsetState = blockAccess.getBlockState(pos.offset(side));
        return offsetState.getBlock() != this && super.shouldSideBeRendered(blockState, blockAccess, pos, side);
    }

}
