package com.yogpc.qp.machines.replacer;

import java.util.List;
import java.util.Optional;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.TranslationKeys;
import com.yogpc.qp.machines.base.QPBlock;
import com.yogpc.qp.utils.Holder;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;


public class BlockReplacer extends QPBlock {

    public BlockReplacer() {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(5f), QuarryPlus.Names.replacer, ItemBlock::new);
        setDefaultState(getStateContainer().getBaseState().with(QPBlock.WORKING(), false));
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
    protected void fillStateContainer(StateContainer.Builder<Block, IBlockState> builder) {
        builder.add(QPBlock.WORKING());
    }


    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(new TextComponentTranslation(TranslationKeys.TOOLTIP_REPLACER,
            new TextComponentTranslation(TranslationKeys.quarry), new TextComponentTranslation(TranslationKeys.advquarry), ' '));
    }

    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) {
        return Holder.replacerType().create();
    }
}
