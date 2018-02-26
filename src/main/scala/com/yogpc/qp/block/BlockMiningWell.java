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
import java.util.List;
import java.util.Optional;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.QuarryPlusI;
import com.yogpc.qp.compat.BuildcraftHelper;
import com.yogpc.qp.compat.EnchantmentHelper;
import com.yogpc.qp.item.ItemBlockEnchantable;
import com.yogpc.qp.tile.IEnchantableTile;
import com.yogpc.qp.tile.TileMiningWell;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockMiningWell extends ADismCBlock {

    private final ArrayList<ItemStack> drops = new ArrayList<>();

    public BlockMiningWell() {
        super(Material.ANVIL, QuarryPlus.Names.miningwell, ItemBlockEnchantable::new);
        setHardness(1.5F);
        setResistance(10F);
        setSoundType(SoundType.STONE);
        setDefaultState(getBlockState().getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(ACTING, false));
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = playerIn.getHeldItem(hand);
        if (BuildcraftHelper.isWrench(playerIn, hand, stack, new RayTraceResult(new Vec3d(hitX, hitY, hitZ), facing, pos))) {
            Optional.ofNullable((TileMiningWell) worldIn.getTileEntity(pos)).ifPresent(TileMiningWell::G_reinit);
            return true;
        }
        if (stack.getItem() == QuarryPlusI.itemTool() && stack.getItemDamage() == 0) {
            if (!worldIn.isRemote) {
                Optional.ofNullable((IEnchantableTile) worldIn.getTileEntity(pos)).ifPresent(t ->
                        EnchantmentHelper.getEnchantmentsChat(t).forEach(playerIn::sendMessage));
            }
            return true;
        }
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, ACTING);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        if (!worldIn.isRemote) {
            EnumFacing facing = get2dOrientation(placer.posX, placer.posZ, pos.getX(), pos.getZ()).getOpposite();
            worldIn.setBlockState(pos, state.withProperty(FACING, facing), 2);
            Optional.ofNullable((IEnchantableTile) worldIn.getTileEntity(pos)).ifPresent(t -> EnchantmentHelper.init(t, stack.getEnchantmentTagList()));
        }
    }

    private static EnumFacing get2dOrientation(final double x1, final double z1, final double x2, final double z2) {
        final double Dx = x1 - x2;
        final double Dz = z1 - z2;
        final double angle = Math.atan2(Dz, Dx) / Math.PI * 180 + 180;

        if (angle < 45 || angle > 315)
            return EnumFacing.EAST;
        else if (angle < 135)
            return EnumFacing.SOUTH;
        else if (angle < 225)
            return EnumFacing.WEST;
        else
            return EnumFacing.NORTH;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        this.drops.clear();
        if (!worldIn.isRemote) {
            TileMiningWell tile = (TileMiningWell) worldIn.getTileEntity(pos);
            if (tile != null) {
                final int count = quantityDropped(state, 0, worldIn.rand);
                final Item it = getItemDropped(state, worldIn.rand, 0);
                for (int i = 0; i < count; i++) {
                    final ItemStack is = new ItemStack(it, 1, damageDropped(state));
                    EnchantmentHelper.enchantmentToIS(tile, is);
                    this.drops.add(is);
                }
            }
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        return drops;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        if (!worldIn.isRemote)
            Optional.ofNullable((TileMiningWell) worldIn.getTileEntity(pos)).ifPresent(TileMiningWell::G_renew_powerConfigure);
    }

    @Override
    public TileMiningWell createNewTileEntity(World worldIn, int meta) {
        return new TileMiningWell();
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        boolean powered = state.getValue(ACTING);
        EnumFacing facing = state.getValue(FACING);
        return facing.getIndex() | (powered ? 8 : 0);
    }

    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.getFront(meta & 7)).withProperty(ACTING, (meta & 8) == 8);
    }

    @Override
    protected boolean canRotate() {
        return true;
    }
}
