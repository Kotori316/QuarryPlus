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
import java.util.function.Consumer;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.compat.BuildcraftHelper;
import com.yogpc.qp.machines.TranslationKeys;
import com.yogpc.qp.machines.base.IEnchantableTile;
import com.yogpc.qp.machines.base.QPBlock;
import com.yogpc.qp.utils.Holder;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
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
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class BlockQuarry extends QPBlock {

    public BlockQuarry() {
        super(Properties.create(Material.IRON)
            .hardnessAndResistance(1.5f, 10f)
            .sound(SoundType.STONE), QuarryPlus.Names.quarry, ItemBlockEnchantable::new);
        setDefaultState(getStateContainer().getBaseState().with(FACING, EnumFacing.NORTH).with(QPBlock.WORKING(), false));
    }

    private final ArrayList<ItemStack> drops = new ArrayList<>();

    @Override
    public void onReplaced(IBlockState state, World worldIn, BlockPos pos, IBlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            this.drops.clear();
            if (!worldIn.isRemote) { // Always true.
                TileQuarry tile = (TileQuarry) worldIn.getTileEntity(pos);
                if (tile != null) {
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
    public boolean onBlockActivated(IBlockState state, World worldIn, BlockPos pos, EntityPlayer player,
                                    EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (super.onBlockActivated(state, worldIn, pos, player, hand, side, hitX, hitY, hitZ)) return true;
        ItemStack stack = player.getHeldItem(hand);
        if (BuildcraftHelper.isWrench(player, hand, stack, new RayTraceResult(new Vec3d(hitX, hitY, hitZ), side, pos))) {
            Optional.ofNullable((TileQuarry) worldIn.getTileEntity(pos)).ifPresent(TileQuarry::G_ReInit);
            return true;
        }
        if (!worldIn.isRemote) {
            TileEntity t = worldIn.getTileEntity(pos);
            if (t != null) {
                TileQuarry quarry = (TileQuarry) t;
                /*if (stack.getItem() == QuarryPlusI.itemTool() && stack.getItemDamage() == ItemTool.meta_StatusChecker()) {
                    quarry.sendEnchantMassage(playerIn);
                    VersionUtil.sendMessage(playerIn, new TextComponentTranslation(TranslationKeys.CURRENT_MODE,
                        new TextComponentTranslation(quarry.filler ? TranslationKeys.FILLER_MODE : TranslationKeys.QUARRY_MODE)));
                } else if (stack.getItem() == QuarryPlusI.itemTool() && stack.getItemDamage() == ItemTool.meta_YSetter()) {
                    playerIn.openGui(QuarryPlus.instance(), QuarryPlusI.guiIdQuarryYLevel(), worldIn, pos.getX(), pos.getY(), pos.getZ());
                } else*/
                if (quarry.G_getNow() == TileQuarry.Mode.NOT_NEED_BREAK) {
                    quarry.filler = !quarry.filler;
                    player.sendStatusMessage(new TextComponentTranslation(TranslationKeys.CHANGEMODE,
                        new TextComponentTranslation(quarry.filler ? TranslationKeys.FILLER_MODE : TranslationKeys.QUARRY_MODE)), false);
                }
            }
        }
        return true;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        if (!worldIn.isRemote) {
            EnumFacing facing = placer.getHorizontalFacing().getOpposite();
            worldIn.setBlockState(pos, state.with(FACING, facing), 2);
            Consumer<TileQuarry> consumer = IEnchantableTile.Util.initConsumer(stack);
            Optional.ofNullable((TileQuarry) worldIn.getTileEntity(pos)).ifPresent(consumer.andThen(TileQuarry.requestTicket));
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        if (!worldIn.isRemote) {
            Optional.ofNullable((TileQuarry) worldIn.getTileEntity(pos)).ifPresent(TileQuarry::G_renew_powerConfigure);
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, IBlockState> builder) {
        builder.add(FACING, QPBlock.WORKING());
    }

    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) {
        return Holder.quarryTileType().create();
    }

}
