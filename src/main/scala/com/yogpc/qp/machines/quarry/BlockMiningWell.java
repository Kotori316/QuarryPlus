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
import com.yogpc.qp.machines.base.IEnchantableTile;
import com.yogpc.qp.machines.base.QPBlock;
import com.yogpc.qp.machines.item.YSetterInteractionObject;
import com.yogpc.qp.utils.Holder;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import static jp.t2v.lab.syntax.MapStreamSyntax.optCast;
import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class BlockMiningWell extends QPBlock {

    private final ArrayList<ItemStack> drops = new ArrayList<>();

    public BlockMiningWell() {
        super(Block.Properties.create(Material.IRON)
                .hardnessAndResistance(1.5f, 10f)
                .sound(SoundType.STONE),
            QuarryPlus.Names.miningwell, ItemBlockEnchantable::new);
        setDefaultState(getStateContainer().getBaseState().with(FACING, EnumFacing.NORTH).with(QPBlock.WORKING(), false));
    }

    @Override
    public boolean onBlockActivated(IBlockState state, World worldIn, BlockPos pos, EntityPlayer player,
                                    EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (super.onBlockActivated(state, worldIn, pos, player, hand, side, hitX, hitY, hitZ)) return true;
        ItemStack stack = player.getHeldItem(hand);
        if (BuildcraftHelper.isWrench(player, hand, stack, new RayTraceResult(new Vec3d(hitX, hitY, hitZ), side, pos))) {
            Optional.ofNullable((TileMiningWell) worldIn.getTileEntity(pos)).ifPresent(TileMiningWell::G_ReInit);
            return true;
        }
        if (stack.getItem() == Holder.itemStatusChecker()) {
            if (!worldIn.isRemote) {
                Optional.ofNullable((IEnchantableTile) worldIn.getTileEntity(pos)).ifPresent(t ->
                    t.sendEnchantMassage(player));
            }
            return true;
        } else if (stack.getItem() == Holder.itemYSetter()) {
            if (!worldIn.isRemote) {
                Optional.ofNullable(worldIn.getTileEntity(pos))
                    .flatMap(optCast(TileMiningWell.class))
                    .ifPresent(t -> NetworkHooks.openGui(((EntityPlayerMP) player), YSetterInteractionObject.apply(t), pos));
            }
            return true;
        }
        return false;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, IBlockState> builder) {
        builder.add(FACING, QPBlock.WORKING());
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        if (!worldIn.isRemote) {
            EnumFacing facing = placer.getAdjustedHorizontalFacing().getOpposite();
            worldIn.setBlockState(pos, state.with(FACING, facing), 2);
            Optional.ofNullable((IEnchantableTile) worldIn.getTileEntity(pos)).ifPresent(IEnchantableTile.Util.initConsumer(stack));
        }
    }

    @Override
    public void onReplaced(IBlockState state, World worldIn, BlockPos pos, IBlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            this.drops.clear();
            if (!worldIn.isRemote) {
                TileMiningWell tile = (TileMiningWell) worldIn.getTileEntity(pos);
                if (tile != null) {
                    tile.removePipes();
                    final int count = getItemsToDropCount(state, 0, worldIn, pos, worldIn.rand);
                    final Item it = getItemDropped(state, worldIn, pos, 0).asItem();
                    for (int i = 0; i < count; i++) {
                        final ItemStack is = new ItemStack(it, 1);
                        IEnchantableTile.Util.enchantmentToIS(tile, is);
                        this.drops.add(is);
                    }
                }
            }
            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public void getDrops(IBlockState state, NonNullList<ItemStack> drops, World world, BlockPos pos, int fortune) {
        drops.addAll(this.drops);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        if (!worldIn.isRemote)
            Optional.ofNullable((TileMiningWell) worldIn.getTileEntity(pos)).ifPresent(TileMiningWell::G_renew_powerConfigure);
    }

    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) {
        return Holder.miningWellTileType().create();
    }
}
