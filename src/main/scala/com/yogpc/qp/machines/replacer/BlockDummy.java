package com.yogpc.qp.machines.replacer;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.utils.Holder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;

public class BlockDummy extends Block {
    private final BlockItem mItemBlock;

    public BlockDummy() {
        super(Block.Properties.create(Material.GLASS)
            .hardnessAndResistance(1f)
            .lightValue(15)
            .noDrops());
        setRegistryName(QuarryPlus.modID, QuarryPlus.Names.dummyblock);
        mItemBlock = new BlockItem(this, new Item.Properties().group(Holder.tab()));
        mItemBlock.setRegistryName(QuarryPlus.modID, QuarryPlus.Names.dummyblock);
    }

    @Override
    public Item asItem() {
        return mItemBlock;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        return true;
    }

    @Override
    public boolean canDropFromExplosion(BlockState state, IBlockReader world, BlockPos pos, Explosion explosion) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isNormalCube(BlockState p_220081_1_, IBlockReader p_220081_2_, BlockPos p_220081_3_) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isSideInvisible(BlockState state, BlockState neighbor, Direction d) {
        return false;
    }
}
