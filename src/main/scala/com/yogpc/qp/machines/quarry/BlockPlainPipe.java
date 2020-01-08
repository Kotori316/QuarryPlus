package com.yogpc.qp.machines.quarry;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.utils.Holder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlockPlainPipe extends Block {
    public static final VoxelShape BOX_AABB = VoxelShapes.create(0.25, 0, 0.25, 0.75, 1, 0.75);
    public final BlockItem itemBlock;

    public BlockPlainPipe() {
        super(Properties.create(Material.GLASS).noDrops());
        setRegistryName(QuarryPlus.modID, QuarryPlus.Names.plainpipe);
        itemBlock = new BlockItem(this, new Item.Properties().group(Holder.tab()));
        itemBlock.setRegistryName(QuarryPlus.modID, QuarryPlus.Names.plainpipe);
    }

    @Override
    public Item asItem() {
        return itemBlock;
    }

    @Override
    @SuppressWarnings("deprecation")
    @OnlyIn(Dist.CLIENT)
    public boolean isSideInvisible(BlockState state, BlockState adjacentBlockState, Direction side) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return BOX_AABB;
    }

}
