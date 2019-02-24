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
import com.yogpc.qp.item.ItemBlockEnchantable;
import com.yogpc.qp.tile.IEnchantableTile;
import com.yogpc.qp.tile.TileLaser;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;

public class BlockLaser extends ADismCBlock {
    private static final AxisAlignedBB NORTH_BASE = new AxisAlignedBB(0, 0, 12d / 16d, 1, 1, 1);
    private static final AxisAlignedBB NORTH_LAUNCHER = new AxisAlignedBB(5d / 16d, 5d / 16d, 3d / 16d, 11d / 16d, 11d / 16d, 12d / 16d);
    private static final AxisAlignedBB SOUTH_BASE = new AxisAlignedBB(0, 0, 0, 1, 1, 4d / 16d);
    private static final AxisAlignedBB SOUTH_LAUNCHER = new AxisAlignedBB(5d / 16d, 5d / 16d, 4d / 16d, 11d / 16d, 11d / 16d, 13d / 16d);

    private static final AxisAlignedBB EAST_BASE = new AxisAlignedBB(0, 0, 0, 4d / 16d, 1, 1);
    private static final AxisAlignedBB EAST_LAUNCHER = new AxisAlignedBB(4d / 16d, 5d / 16d, 5d / 16d, 13d / 16d, 11d / 16d, 11d / 16d);
    private static final AxisAlignedBB WEST_BASE = new AxisAlignedBB(12d / 16d, 0, 0, 1, 1, 1);
    private static final AxisAlignedBB WEST_LAUNCHER = new AxisAlignedBB(3d / 16d, 5d / 16d, 5d / 16d, 12d / 16d, 11d / 16d, 11d / 16d);

    private static final AxisAlignedBB UP_BASE = new AxisAlignedBB(0, 0, 0, 1, 4d / 16d, 1);
    private static final AxisAlignedBB UP_LAUNCHER = new AxisAlignedBB(5d / 16d, 4d / 16d, 5d / 16d, 11d / 16d, 13d / 16d, 11d / 16d);
    private static final AxisAlignedBB DOWN_BASE = new AxisAlignedBB(0, 12d / 16d, 0, 1, 1, 1);
    private static final AxisAlignedBB DOWN_LAUNCHER = new AxisAlignedBB(5d / 16d, 3d / 16d, 5d / 16d, 11d / 16d, 12d / 16d, 11d / 16d);
    private final boolean bcLoaded;

    public BlockLaser() {
        super(Material.IRON, QuarryPlus.Names.laser, ItemBlockEnchantable::new);
        setHardness(10F);
        setDefaultState(getBlockState().getBaseState().withProperty(FACING, EnumFacing.UP));
        bcLoaded = Loader.isModLoaded(QuarryPlus.Optionals.Buildcraft_silicon_modID);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox,
                                      List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean p_185477_7_) {
        EnumFacing value = state.getValue(FACING);
        switch (value) {
            case DOWN:
                addCollisionBoxToList(pos, entityBox, collidingBoxes, DOWN_BASE);
                addCollisionBoxToList(pos, entityBox, collidingBoxes, DOWN_LAUNCHER);
                break;
            case UP:
                addCollisionBoxToList(pos, entityBox, collidingBoxes, UP_BASE);
                addCollisionBoxToList(pos, entityBox, collidingBoxes, UP_LAUNCHER);
                break;
            case NORTH:
                addCollisionBoxToList(pos, entityBox, collidingBoxes, NORTH_BASE);
                addCollisionBoxToList(pos, entityBox, collidingBoxes, NORTH_LAUNCHER);
                break;
            case SOUTH:
                addCollisionBoxToList(pos, entityBox, collidingBoxes, SOUTH_BASE);
                addCollisionBoxToList(pos, entityBox, collidingBoxes, SOUTH_LAUNCHER);
                break;
            case WEST:
                addCollisionBoxToList(pos, entityBox, collidingBoxes, WEST_BASE);
                addCollisionBoxToList(pos, entityBox, collidingBoxes, WEST_LAUNCHER);
                break;
            case EAST:
                addCollisionBoxToList(pos, entityBox, collidingBoxes, EAST_BASE);
                addCollisionBoxToList(pos, entityBox, collidingBoxes, EAST_LAUNCHER);
                break;
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        switch (state.getValue(FACING)) {
            case DOWN:
                return DOWN_BASE.union(DOWN_LAUNCHER);
            case UP:
                return UP_BASE.union(UP_LAUNCHER);
            case NORTH:
                return NORTH_BASE.union(NORTH_LAUNCHER);
            case SOUTH:
                return SOUTH_BASE.union(SOUTH_LAUNCHER);
            case WEST:
                return WEST_BASE.union(WEST_LAUNCHER);
            case EAST:
                return EAST_BASE.union(EAST_LAUNCHER);
        }
        return super.getBoundingBox(state, source, pos); // unreachable
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        EnumFacing facing = state.getValue(FACING);
        return facing.getIndex();
    }

    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.getFront(meta));
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public TileLaser createNewTileEntity(final World w, final int m) {
        return new TileLaser();
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing,
                                            float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return getDefaultState().withProperty(FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer));
    }


    private final ArrayList<ItemStack> drop = new ArrayList<>();

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        Optional<TileLaser> tileEntity = Optional.ofNullable((TileLaser) worldIn.getTileEntity(pos));
        this.drop.clear();
        tileEntity.ifPresent(tile -> {
            final int count = quantityDropped(state, 0, worldIn.rand);
            final Item it = getItemDropped(state, worldIn.rand, 0);
            for (int i = 0; i < count; i++) {
                final ItemStack is = new ItemStack(it, 1, damageDropped(state));
                IEnchantableTile.Util.enchantmentToIS(tile, is);
                this.drop.add(is);
            }
        });
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        drops.addAll(this.drop);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        Optional.ofNullable((IEnchantableTile) worldIn.getTileEntity(pos)).ifPresent(t -> IEnchantableTile.Util.init(t, stack.getEnchantmentTagList()));
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = playerIn.getHeldItem(hand);
        if (stack.getItem() == QuarryPlusI.itemTool() && stack.getItemDamage() == 0) {
            if (!worldIn.isRemote) {
                Optional.ofNullable((IEnchantableTile) worldIn.getTileEntity(pos)).ifPresent(t -> t.sendEnchantMassage(playerIn));
            }
            return true;
        }
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        if (bcLoaded)
            super.getSubBlocks(itemIn, items);
    }

    @Override
    protected boolean canRotate() {
        return false;
    }
}
