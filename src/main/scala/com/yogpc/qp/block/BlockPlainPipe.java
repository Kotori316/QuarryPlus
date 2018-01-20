package com.yogpc.qp.block;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.QuarryPlusI;
import net.minecraft.block.BlockEmptyDrops;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockPlainPipe extends BlockEmptyDrops {
    public static final AxisAlignedBB BOX_AABB = new AxisAlignedBB(0.25, 0, 0.25, 0.75, 1, 0.75);
    public final ItemBlock itemBlock;

    public BlockPlainPipe() {
        super(Material.GLASS);
        setCreativeTab(QuarryPlusI.creativeTab());
        setUnlocalizedName(QuarryPlus.Names.plainpipe);
        setRegistryName(QuarryPlus.modID, QuarryPlus.Names.plainpipe);
        itemBlock = new ItemBlock(this);
        itemBlock.setRegistryName(QuarryPlus.modID, QuarryPlus.Names.plainpipe);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    @SuppressWarnings("deprecation")
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOX_AABB;
    }
}
