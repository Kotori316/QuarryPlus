package com.yogpc.qp.block;

import java.util.Optional;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.tile.TileReplacer;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static com.yogpc.qp.block.BlockPump.CONNECTED;

public class BlockReplacer extends QPBlock {

    public BlockReplacer() {
        super(Material.IRON, QuarryPlus.Names.replacer, ItemBlock::new);
        setHardness(5F);
        setDefaultState(getBlockState().getBaseState().withProperty(CONNECTED, false));
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileReplacer();
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        Optional.ofNullable((TileReplacer) worldIn.getTileEntity(pos)).ifPresent(TileReplacer::onPlaced);
    }

    @SuppressWarnings({"deprecation"})
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        Optional.ofNullable((TileReplacer) worldIn.getTileEntity(pos)).ifPresent(TileReplacer::neighborChanged);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, CONNECTED);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        boolean connected = state.getValue(CONNECTED);
        return (connected ? 2 : 0);
    }

    @SuppressWarnings("deprecation")
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(CONNECTED, (meta & 2) == 2);
    }

}
