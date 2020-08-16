/*
 * Copyright (C) 2012,2013 yogpstop This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.yogpc.qp.machines.pump;

import java.util.List;
import java.util.Optional;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.compat.BuildcraftHelper;
import com.yogpc.qp.machines.base.IEnchantableTile;
import com.yogpc.qp.machines.base.QPBlock;
import com.yogpc.qp.utils.Holder;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import static net.minecraft.state.properties.BlockStateProperties.ENABLED;

public class BlockPump extends QPBlock {

    public BlockPump() {
        super(Properties.create(Material.IRON).hardnessAndResistance(3.0f), QuarryPlus.Names.pump, BlockItemPump::new);
        setDefaultState(getStateContainer().getBaseState().with(QPBlock.WORKING(), false).with(ENABLED, false));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(QPBlock.WORKING(), ENABLED);
    }

    @Override
    public TileEntityType<? extends TileEntity> getTileType() {
        return Holder.pumpTileType();
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        Optional.ofNullable((IEnchantableTile) worldIn.getTileEntity(pos)).ifPresent(IEnchantableTile.Util.initConsumer(stack));
    }

    @Override
    @SuppressWarnings({"deprecation"})
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean b) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos, b);
        Optional.ofNullable((TilePump) worldIn.getTileEntity(pos)).ifPresent(TilePump::G_ReInit);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (super.onBlockActivated(state, worldIn, pos, player, hand, hit).isSuccess()) return ActionResultType.SUCCESS;
        ItemStack stack = player.getHeldItem(hand);
        if (BuildcraftHelper.isWrench(player, hand, stack, hit)) {
            if (!worldIn.isRemote)
                Optional.ofNullable((TilePump) worldIn.getTileEntity(pos)).ifPresent(pump -> pump.S_changeRange(player));
            return ActionResultType.SUCCESS;
        }
        if (stack.getItem() == Holder.itemStatusChecker()) {
            if (!worldIn.isRemote) {
                TilePump pump = (TilePump) worldIn.getTileEntity(pos);
                if (pump != null) {
                    pump.sendEnchantMassage(player);
                    pump.C_getNames().forEach(c -> player.sendStatusMessage(c, false));
                }
            }
            return ActionResultType.SUCCESS;
        } else if (stack.getItem() == Holder.itemLiquidSelector()) {
            if (!worldIn.isRemote) {
                TilePump pump = (TilePump) worldIn.getTileEntity(pos);
                if (pump != null) {
//                QuarryPlus.proxy.openPumpGui(worldIn, playerIn, facing, pump);
                }
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
//        tooltip.add(new TranslationTextComponent(TranslationKeys.TOOLTIP_PUMP, new TranslationTextComponent(TranslationKeys.quarry), ' '));
    }

}
