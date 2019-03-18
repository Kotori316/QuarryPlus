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

package com.yogpc.qp.block;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.QuarryPlusI;
import com.yogpc.qp.compat.BuildcraftHelper;
import com.yogpc.qp.gui.TranslationKeys;
import com.yogpc.qp.item.ItemBlockEnchantable;
import com.yogpc.qp.item.ItemTool;
import com.yogpc.qp.tile.IEnchantableTile;
import com.yogpc.qp.tile.TileQuarry;
import com.yogpc.qp.version.VersionUtil;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockQuarry extends ADismCBlock {

    public BlockQuarry() {
        super(Material.IRON, QuarryPlus.Names.quarry, ItemBlockEnchantable::new);
        setHardness(1.5F);
        setResistance(10F);
        setSoundType(SoundType.STONE);
        setDefaultState(getBlockState().getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(ACTING, false));
    }

    private final ArrayList<ItemStack> drops = new ArrayList<>();

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        this.drops.clear();
        if (!worldIn.isRemote) { // Always true.
            TileQuarry tile = (TileQuarry) worldIn.getTileEntity(pos);
            if (tile != null) {
                addEnchantedItem(worldIn, state, tile, this.drops);
            }
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        drops.addAll(this.drops);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ)) return true;
        ItemStack stack = playerIn.getHeldItem(hand);
        if (BuildcraftHelper.isWrench(playerIn, hand, stack, new RayTraceResult(new Vec3d(hitX, hitY, hitZ), facing, pos))) {
            Optional.ofNullable((TileQuarry) worldIn.getTileEntity(pos)).ifPresent(TileQuarry::G_ReInit);
            return true;
        }
        if (!worldIn.isRemote) {
            TileEntity t = worldIn.getTileEntity(pos);
            if (t != null) {
                TileQuarry quarry = (TileQuarry) t;
                if (stack.getItem() == QuarryPlusI.itemTool() && stack.getItemDamage() == ItemTool.meta_StatusChecker()) {
                    quarry.sendEnchantMassage(playerIn);
                    VersionUtil.sendMessage(playerIn, new TextComponentTranslation(TranslationKeys.CURRENT_MODE,
                        new TextComponentTranslation(quarry.filler ? TranslationKeys.FILLER_MODE : TranslationKeys.QUARRY_MODE)));
                } else if (stack.getItem() == QuarryPlusI.itemTool() && stack.getItemDamage() == ItemTool.meta_YSetter()) {
                    playerIn.openGui(QuarryPlus.instance(), QuarryPlusI.guiIdQuarryYLevel(), worldIn, pos.getX(), pos.getY(), pos.getZ());
                } else if (quarry.G_getNow() == TileQuarry.Mode.NOT_NEED_BREAK) {
                    quarry.filler = !quarry.filler;
                    VersionUtil.sendMessage(playerIn, new TextComponentTranslation(TranslationKeys.CHANGEMODE,
                        new TextComponentTranslation(quarry.filler ? TranslationKeys.FILLER_MODE : TranslationKeys.QUARRY_MODE)));
                }
            }
        }
        return true;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        if (!worldIn.isRemote) {
            EnumFacing facing = get2dOrientation(placer.posX, placer.posZ, pos.getX(), pos.getZ()).getOpposite();
            worldIn.setBlockState(pos, state.withProperty(FACING, facing), 2);
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
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, ACTING);
    }

    @Override
    public TileQuarry createNewTileEntity(World worldIn, int meta) {
        return new TileQuarry();
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        boolean powered = state.getValue(ACTING);
        EnumFacing facing = state.getValue(FACING);
        return facing.getIndex() | (powered ? 8 : 0);
    }

    @SuppressWarnings("deprecation")
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.getFront(meta & 7)).withProperty(ACTING, (meta & 8) == 8);
    }

    @Override
    protected boolean canRotate() {
        return true;
    }
}
