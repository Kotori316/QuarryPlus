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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.compat.BuildcraftHelper;
import com.yogpc.qp.machines.TranslationKeys;
import com.yogpc.qp.machines.base.IEnchantableTile;
import com.yogpc.qp.machines.base.QPBlock;
import com.yogpc.qp.utils.Holder;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import static net.minecraft.state.properties.BlockStateProperties.ENABLED;

public class BlockPump extends QPBlock {

    public BlockPump() {
        super(Properties.create(Material.IRON).hardnessAndResistance(5f), QuarryPlus.Names.pump, ItemBlockPump::new);
        setDefaultState(getStateContainer().getBaseState().with(QPBlock.WORKING(), false).with(ENABLED, false));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, IBlockState> builder) {
        builder.add(QPBlock.WORKING(), ENABLED);
    }

    private final ArrayList<ItemStack> drop = new ArrayList<>();

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) {
        return Holder.pumpTileType().create();
    }

    @Override
    public void onReplaced(IBlockState state, World worldIn, BlockPos pos, IBlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            this.drop.clear();
            Optional.ofNullable((TilePump) worldIn.getTileEntity(pos)).ifPresent(pump -> {
                ItemStack stack = new ItemStack(this, 1);
                IEnchantableTile.Util.enchantmentToIS(pump, stack);
                drop.add(stack);
            });
            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public void getDrops(IBlockState state, NonNullList<ItemStack> drops, World world, BlockPos pos, int fortune) {
        drops.addAll(this.drop);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        Optional.ofNullable((IEnchantableTile) worldIn.getTileEntity(pos)).ifPresent(IEnchantableTile.Util.initConsumer(stack));
    }

    @SuppressWarnings({"deprecation"})
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        Optional.ofNullable((TilePump) worldIn.getTileEntity(pos)).ifPresent(TilePump::G_ReInit);
    }

    @Override
    public boolean onBlockActivated(IBlockState state, World worldIn, BlockPos pos, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (super.onBlockActivated(state, worldIn, pos, player, hand, side, hitX, hitY, hitZ)) return true;
        ItemStack stack = player.getHeldItem(hand);
        if (BuildcraftHelper.isWrench(player, hand, stack, new RayTraceResult(new Vec3d(hitX, hitY, hitZ), side, pos))) {
            if (!worldIn.isRemote)
                Optional.ofNullable((TilePump) worldIn.getTileEntity(pos)).ifPresent(pump -> pump.S_changeRange(player));
            return true;
        }
        if (stack.getItem() == Holder.itemStatusChecker()) {
            if (!worldIn.isRemote) {
                TilePump pump = (TilePump) worldIn.getTileEntity(pos);
                if (pump != null) {
                    pump.sendEnchantMassage(player);
                    pump.C_getNames().forEach(c -> player.sendStatusMessage(c, false));
                }
            }
            return true;
        } else if (stack.getItem() == Holder.itemLiquidSelector()) {
            if(!worldIn.isRemote){
                TilePump pump = (TilePump) worldIn.getTileEntity(pos);
                if (pump != null) {
//                QuarryPlus.proxy.openPumpGui(worldIn, playerIn, facing, pump);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(new TextComponentTranslation(TranslationKeys.TOOLTIP_PUMP, new TextComponentTranslation(TranslationKeys.quarry), ' '));
    }

}
