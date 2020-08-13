package com.yogpc.qp.machines.replacer;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.utils.Holder;
import net.minecraft.block.AbstractGlassBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;

public class BlockDummy extends AbstractGlassBlock {
    private final BlockItem mItemBlock;

    public BlockDummy() {
        super(Block.Properties.create(Material.GLASS)
            .hardnessAndResistance(1f)
            .setLightLevel(value -> 15)
            .noDrops()
            .sound(SoundType.GLASS)
            .setAllowsSpawn((state, world, pos, entityType) -> false)
            .notSolid());
        setRegistryName(QuarryPlus.modID, QuarryPlus.Names.dummyblock);
        mItemBlock = new BlockItem(this, new Item.Properties().group(Holder.tab()));
        mItemBlock.setRegistryName(QuarryPlus.modID, QuarryPlus.Names.dummyblock);
    }

    @Override
    public Item asItem() {
        return mItemBlock;
    }

    @Override
    public boolean canDropFromExplosion(BlockState state, IBlockReader world, BlockPos pos, Explosion explosion) {
        return false;
    }

}
