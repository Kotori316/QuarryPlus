package com.yogpc.qp.machines.replacer;

import java.util.List;
import java.util.Optional;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.base.QPBlock;
import com.yogpc.qp.utils.Holder;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BlockReplacer extends QPBlock {

    public BlockReplacer() {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(3.0f), QuarryPlus.Names.replacer, BlockItem::new);
        setDefaultState(getStateContainer().getBaseState().with(QPBlock.WORKING(), false));
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        Optional.ofNullable((TileReplacer) worldIn.getTileEntity(pos)).ifPresent(TileReplacer::onPlaced);
    }

    @SuppressWarnings({"deprecation"})
    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean b) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos, b);
        Optional.ofNullable((TileReplacer) worldIn.getTileEntity(pos)).ifPresent(TileReplacer::neighborChanged);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(QPBlock.WORKING());
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
//        tooltip.add(new TranslationTextComponent(TranslationKeys.TOOLTIP_REPLACER,
//            new TranslationTextComponent(TranslationKeys.quarry), new TranslationTextComponent(TranslationKeys.advquarry), ' '));
    }

    @Override
    public TileEntityType<? extends TileEntity> getTileType() {
        return Holder.replacerType();
    }
}
