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

package com.yogpc.qp.machines.quarry;

import java.util.ArrayList;
import java.util.Optional;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.compat.BuildcraftHelper;
import com.yogpc.qp.integration.ftbchunks.QuarryChunkProtectionManager;
import com.yogpc.qp.machines.base.IEnchantableTile;
import com.yogpc.qp.machines.base.QPBlock;
import com.yogpc.qp.utils.Holder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class BlockMiningWell extends QPBlock {

    private final ArrayList<ItemStack> drops = new ArrayList<>();

    public BlockMiningWell() {
        super(Block.Properties.create(Material.IRON)
                .hardnessAndResistance(1.5f, 10f)
                .sound(SoundType.STONE),
            QuarryPlus.Names.miningwell, BlockItemEnchantable::new);
        setDefaultState(getStateContainer().getBaseState().with(FACING, Direction.NORTH).with(QPBlock.WORKING(), false));
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player,
                                             Hand hand, BlockRayTraceResult hit) {
        if (super.onBlockActivated(state, worldIn, pos, player, hand, hit).isSuccess()) return ActionResultType.SUCCESS;
        ItemStack stack = player.getHeldItem(hand);
        if (BuildcraftHelper.isWrench(player, hand, stack, hit)) {
            Optional.ofNullable((TileMiningWell) worldIn.getTileEntity(pos)).ifPresent(TileMiningWell::G_ReInit);
            return ActionResultType.SUCCESS;
        }
        if (stack.getItem() == Holder.itemStatusChecker()) {
            if (!worldIn.isRemote) {
                Optional.ofNullable((IEnchantableTile) worldIn.getTileEntity(pos)).ifPresent(t ->
                    t.sendEnchantMassage(player));
            }
            return ActionResultType.SUCCESS;
        } else if (stack.getItem() == Holder.itemYSetter()) {
            if (!worldIn.isRemote) {
//                Optional.ofNullable(worldIn.getTileEntity(pos))
//                    .flatMap(optCast(TileMiningWell.class))
//                    .ifPresent(t -> NetworkHooks.openGui(((ServerPlayerEntity) player), YSetterInteractionObject.apply(t, pos), pos));
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, QPBlock.WORKING());
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (!worldIn.isRemote) {
            Direction facing = placer.getAdjustedHorizontalFacing().getOpposite();
            worldIn.setBlockState(pos, state.with(FACING, facing), 2);
            Optional.ofNullable((TileMiningWell) worldIn.getTileEntity(pos)).ifPresent(IEnchantableTile.Util.<TileMiningWell>initConsumer(stack)
                .andThen(QuarryChunkProtectionManager.minerSendProtectionNotification(placer)));
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            this.drops.clear();
            if (!worldIn.isRemote) {
                TileMiningWell tile = (TileMiningWell) worldIn.getTileEntity(pos);
                if (tile != null) {
                    tile.removePipes();
                }
            }
            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean b) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos, b);
        if (!worldIn.isRemote)
            Optional.ofNullable((TileMiningWell) worldIn.getTileEntity(pos)).ifPresent(TileMiningWell::G_renew_powerConfigure);
    }

    @Override
    public TileEntityType<? extends TileEntity> getTileType() {
        return Holder.miningWellTileType();
    }
}
