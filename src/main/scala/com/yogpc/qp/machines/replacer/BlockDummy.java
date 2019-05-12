package com.yogpc.qp.machines.replacer;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.utils.Holder;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEmptyDrops;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.IWorldReaderBase;

public class BlockDummy extends BlockEmptyDrops {
    private final ItemBlock mItemBlock;

    public BlockDummy() {
        super(Block.Properties.create(Material.GLASS)
            .hardnessAndResistance(1f)
            .lightValue(15));
        setRegistryName(QuarryPlus.modID, QuarryPlus.Names.dummyblock);
        mItemBlock = new ItemBlock(this, new Item.Properties().group(Holder.tab()));
        mItemBlock.setRegistryName(QuarryPlus.modID, QuarryPlus.Names.dummyblock);
    }

    @Override
    public Item asItem() {
        return mItemBlock;
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IWorldReaderBase world, BlockPos pos,
                                    EntitySpawnPlacementRegistry.SpawnPlacementType type,
                                    @Nullable EntityType<? extends EntityLiving> entityType) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean canEntitySpawn(IBlockState state, Entity entityIn) {
        return false;
    }

    @Override
    public boolean canSilkHarvest(IBlockState state, IWorldReader world, BlockPos pos, EntityPlayer player) {
        return false;
    }

    @Override
    public boolean propagatesSkylightDown(IBlockState state, IBlockReader reader, BlockPos pos) {
        return true;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isSideInvisible(IBlockState state, IBlockState adjacentBlockState, EnumFacing side) {
        return adjacentBlockState.getBlock() == this || super.isSideInvisible(state, adjacentBlockState, side);
    }

    @Override
    public boolean canDropFromExplosion(Explosion explosionIn) {
        return false;
    }
}
