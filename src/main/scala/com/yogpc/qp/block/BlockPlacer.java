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

import java.util.Optional;
import java.util.Random;

import com.yogpc.qp.Config;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.QuarryPlusI;
import com.yogpc.qp.compat.BuildcraftHelper;
import com.yogpc.qp.compat.InvUtils;
import com.yogpc.qp.tile.TilePlacer;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import scala.Symbol;

public class BlockPlacer extends ADismCBlock {
    public static final Symbol SYMBOL = Symbol.apply("PlacerPlus");

    public BlockPlacer() {
        super(Material.IRON, QuarryPlus.Names.placer, ItemBlock::new);
        setHardness(3.5F);
        setSoundType(SoundType.STONE);
        //Random tick setting is in Config.
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(POWERED, false));
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        super.updateTick(worldIn, pos, state, rand);
        if (!worldIn.isRemote && !Config.content().disableMapJ().get(SYMBOL)) {
            Optional.ofNullable((TilePlacer) worldIn.getTileEntity(pos)).ifPresent(TilePlacer::updateTick);
        }
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, POWERED);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        if (!worldIn.isRemote) {
            worldIn.setBlockState(pos, state.withProperty(FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer)), 2);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos p_193383_3_, EnumFacing side) {
        return state.getValue(FACING) != side ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        boolean flag = worldIn.isBlockPowered(pos);

        if (flag != state.getValue(POWERED)) {
            state = state.withProperty(POWERED, flag);
            TileEntity entity = worldIn.getTileEntity(pos);
            if (entity != null) {
                entity.validate();
                worldIn.setBlockState(pos, state.withProperty(POWERED, flag), 2);
                entity.validate();
                worldIn.setTileEntity(pos, entity);
            }
            if (flag)
                updateTick(worldIn, pos, state, worldIn.rand);
        }
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ)) return true;
        ItemStack stack = playerIn.getHeldItem(hand);
        if (BuildcraftHelper.isWrench(playerIn, hand, stack, new RayTraceResult(new Vec3d(hitX, hitY, hitZ), facing, pos))) {
            return InvUtils.setNewState(worldIn, pos, state.cycleProperty(FACING));
        } else if (!playerIn.isSneaking()) {
            if (Config.content().debug()) {
                QuarryPlus.LOGGER.info("Placer touched " + hand + " Item : " + stack);
            }
            playerIn.openGui(QuarryPlus.INSTANCE, QuarryPlusI.guiIdPlacer(), worldIn, pos.getX(), pos.getY(), pos.getZ());
            return true;
        }
        return false;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        InvUtils.dropAndUpdateInv(worldIn, pos, (TilePlacer) worldIn.getTileEntity(pos), this);
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public TilePlacer createNewTileEntity(World worldIn, int meta) {
        return new TilePlacer();
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        boolean powered = state.getValue(POWERED);
        EnumFacing facing = state.getValue(FACING);
        return facing.getIndex() | (powered ? 8 : 0);
    }

    @SuppressWarnings("deprecation")
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.getFront(meta & 7)).withProperty(POWERED, (meta & 8) == 8);
    }

    @Override
    protected boolean canRotate() {
        return true;
    }

    @Override
    protected boolean validFacing(EnumFacing facing) {
        return true;
    }
}
