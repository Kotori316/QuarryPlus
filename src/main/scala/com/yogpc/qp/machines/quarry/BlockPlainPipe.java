package com.yogpc.qp.machines.quarry;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.utils.Holder;
import net.minecraft.block.BlockEmptyDrops;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlockPlainPipe extends BlockEmptyDrops {
    public static final VoxelShape BOX_AABB = VoxelShapes.create(0.25, 0, 0.25, 0.75, 1, 0.75);
    public final ItemBlock itemBlock;

    public BlockPlainPipe() {
        super(Properties.create(Material.GLASS));
        setRegistryName(QuarryPlus.modID, QuarryPlus.Names.plainpipe);
        itemBlock = new ItemBlock(this, new Item.Properties().group(Holder.tab()));
        itemBlock.setRegistryName(QuarryPlus.modID, QuarryPlus.Names.plainpipe);
    }

    @Override
    public Item asItem() {
        return itemBlock;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    @SuppressWarnings("deprecation")
    @OnlyIn(Dist.CLIENT)
    public boolean isSideInvisible(IBlockState state, IBlockState adjacentBlockState, EnumFacing side) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(IBlockState state, IBlockReader worldIn, BlockPos pos) {
        return BOX_AABB;
    }

}
