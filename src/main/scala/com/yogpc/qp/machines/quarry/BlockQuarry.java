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

import java.util.Optional;
import java.util.function.Consumer;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.compat.BuildcraftHelper;
import com.yogpc.qp.compat.InvUtils;
import com.yogpc.qp.machines.TranslationKeys;
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
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class BlockQuarry extends QPBlock {

    public BlockQuarry() {
        super(Properties.create(Material.IRON)
            .hardnessAndResistance(1.5f, 10f)
            .sound(SoundType.STONE), QuarryPlus.Names.quarry, BlockItemEnchantable::new);
        setDefaultState(getStateContainer().getBaseState().with(FACING, Direction.NORTH).with(QPBlock.WORKING(), false));
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player,
                                    Hand hand, BlockRayTraceResult hit) {
        if (InvUtils.isDebugItem(player, hand)) return true;
        ItemStack stack = player.getHeldItem(hand);
        if (BuildcraftHelper.isWrench(player, hand, stack, hit)) {
            Optional.ofNullable((TileQuarry) worldIn.getTileEntity(pos)).ifPresent(TileQuarry::G_ReInit);
            return true;
        }
        if (!worldIn.isRemote) {
            TileEntity t = worldIn.getTileEntity(pos);
            if (t != null) {
                TileQuarry quarry = (TileQuarry) t;
                if (stack.getItem() == Holder.itemStatusChecker()) {
                    quarry.sendEnchantMassage(player);
                    player.sendStatusMessage(new TranslationTextComponent(TranslationKeys.CURRENT_MODE,
                        new TranslationTextComponent(quarry.filler ? TranslationKeys.FILLER_MODE : TranslationKeys.QUARRY_MODE)), false);
                } else if (stack.getItem() == Holder.itemYSetter()) {
//                    NetworkHooks.openGui(((ServerPlayerEntity) player), YSetterInteractionObject.apply(quarry, pos), pos);
                } else if (quarry.G_getNow() == TileQuarry.Mode.NOT_NEED_BREAK) {
                    quarry.filler = !quarry.filler;
                    player.sendStatusMessage(new TranslationTextComponent(TranslationKeys.CHANGEMODE,
                        new TranslationTextComponent(quarry.filler ? TranslationKeys.FILLER_MODE : TranslationKeys.QUARRY_MODE)), false);
                }
            }
        }
        return true;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        if (!worldIn.isRemote) {
            Direction facing = placer.getHorizontalFacing().getOpposite();
            worldIn.setBlockState(pos, state.with(FACING, facing), 2);
            Consumer<TileQuarry> consumer = IEnchantableTile.Util.initConsumer(stack);
            Optional.ofNullable((TileQuarry) worldIn.getTileEntity(pos)).ifPresent(consumer.andThen(TileQuarry.requestTicket));
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean b) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos, b);
        if (!worldIn.isRemote) {
            Optional.ofNullable((TileQuarry) worldIn.getTileEntity(pos)).ifPresent(TileQuarry::G_renew_powerConfigure);
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, QPBlock.WORKING());
    }

    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) {
        return Holder.quarryTileType().create();
    }

}
