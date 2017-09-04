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

import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.QuarryPlusI;
import com.yogpc.qp.tile.TilePump;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockFrame extends Block {

    /**
     * Whether this fence connects in the northern direction
     */
    public static final PropertyBool NORTH = PropertyBool.create("north");
    public static final PropertyBool EAST = PropertyBool.create("east");
    public static final PropertyBool SOUTH = PropertyBool.create("south");
    public static final PropertyBool WEST = PropertyBool.create("west");
    public static final PropertyBool TOP = PropertyBool.create("top");
    public static final PropertyBool DOWN = PropertyBool.create("down");
    public static final PropertyBool DAMMING = PropertyBool.create("damming");
    public static final AxisAlignedBB BOX_AABB = new AxisAlignedBB(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);
    public static final AxisAlignedBB North_AABB = new AxisAlignedBB(0.25, 0.25, 0, 0.75, 0.75, 0.25);
    public static final AxisAlignedBB South_AABB = new AxisAlignedBB(0.25, 0.25, .75, 0.75, 0.75, 1);
    public static final AxisAlignedBB West_AABB = new AxisAlignedBB(0, 0.25, 0.25, .25, 0.75, 0.75);
    public static final AxisAlignedBB East_AABB = new AxisAlignedBB(.75, 0.25, 0.25, 1, 0.75, 0.75);
    public static final AxisAlignedBB UP_AABB = new AxisAlignedBB(0.25, .75, 0.25, 0.75, 1, 0.75);
    public static final AxisAlignedBB Down_AABB = new AxisAlignedBB(0.25, 0, 0.25, 0.75, .25, 0.75);

    public final ItemBlock itemBlock;

    public BlockFrame() {
        super(Material.GLASS);
        setHardness(0.5F);
        setCreativeTab(QuarryPlusI.ct);
        setUnlocalizedName(QuarryPlus.Names.frame);
        setRegistryName(QuarryPlus.modID, QuarryPlus.Names.frame);
        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(NORTH, false).withProperty(EAST, false).withProperty(SOUTH, false)
                .withProperty(WEST, false).withProperty(TOP, false).withProperty(DOWN, false)
                .withProperty(DAMMING, false));
        itemBlock = new ItemBlock(this);
        itemBlock.setRegistryName(QuarryPlus.modID, QuarryPlus.Names.frame);
    }

    /**
     * Get the actual Block state of this Block at the given position. This applies properties not visible in the
     * metadata, such as fence connections.
     */
    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return state
                .withProperty(NORTH, canConnectTo(worldIn, pos.north()))
                .withProperty(EAST, canConnectTo(worldIn, pos.east()))
                .withProperty(SOUTH, canConnectTo(worldIn, pos.south()))
                .withProperty(WEST, canConnectTo(worldIn, pos.west()))
                .withProperty(DOWN, canConnectTo(worldIn, pos.down()))
                .withProperty(TOP, canConnectTo(worldIn, pos.up()));
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        boolean d = Stream.of(EnumFacing.VALUES).anyMatch(facing -> {
            IBlockState blockState = world.getBlockState(pos.offset(facing));
            return !blockState.isFullCube() && TilePump.isLiquid(blockState, false, world, pos.offset(facing));
        });
        if (!world.isRemote && !d) {
            removeNeighboringFrames(world, pos);
        }
    }

    /**
     * copied from buildcraft frame.
     */
    public void removeNeighboringFrames(World world, BlockPos pos) {
        for (EnumFacing dir : EnumFacing.VALUES) {
            BlockPos nPos = pos.offset(dir);
            IBlockState nBlock = world.getBlockState(nPos);
            if (nBlock.getBlock() == this) {
                neighborChanged(nBlock, world, nPos, QuarryPlusI.blockFrame, nPos);
                if (!world.getBlockState(nPos).getValue(DAMMING))
                    world.setBlockToAir(nPos);
            }
        }
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(DAMMING) ? 1 : 0;
    }

    private boolean canConnectTo(IBlockAccess worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos).getBlock() == this;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, NORTH, EAST, WEST, SOUTH, TOP, DOWN, DAMMING);
    }

    @Override
    @SuppressWarnings("deprecation")
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getStateFromMeta(int meta) {
        return super.getStateFromMeta(meta).withProperty(DAMMING, meta == 1);
    }

    public IBlockState getDamiingState() {
        return getDefaultState().withProperty(DAMMING, true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
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
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return null;
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    @Override
    @SuppressWarnings("deprecation")
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        state = state.getActualState(source, pos);
        AxisAlignedBB aabb = BOX_AABB;
        if (state.getValue(NORTH)) {
            aabb = aabb.union(North_AABB);
        }
        if (state.getValue(SOUTH)) {
            aabb = aabb.union(South_AABB);
        }
        if (state.getValue(WEST)) {
            aabb = aabb.union(West_AABB);
        }
        if (state.getValue(EAST)) {
            aabb = aabb.union(East_AABB);
        }
        if (state.getValue(TOP)) {
            aabb = aabb.union(UP_AABB);
        }
        if (state.getValue(DOWN)) {
            aabb = aabb.union(Down_AABB);
        }
        return aabb;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox,
                                      List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean p_185477_7_) {
        state = state.getActualState(worldIn, pos);
        addCollisionBoxToList(pos, entityBox, collidingBoxes, BOX_AABB);
        if (state.getValue(NORTH)) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, North_AABB);
        }
        if (state.getValue(SOUTH)) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, South_AABB);
        }
        if (state.getValue(WEST)) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, West_AABB);
        }
        if (state.getValue(EAST)) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, East_AABB);
        }
        if (state.getValue(TOP)) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, UP_AABB);
        }
        if (state.getValue(DOWN)) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, Down_AABB);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        if (state.getValue(DAMMING)) {
            worldIn.setBlockState(pos, state.withProperty(DAMMING,
                    Stream.of(EnumFacing.VALUES).anyMatch(facing -> {
                        IBlockState blockState = worldIn.getBlockState(pos.offset(facing));
                        return !blockState.isFullCube() && TilePump.isLiquid(blockState, false, worldIn, pos.offset(facing));
                    })));
        }
    }
}
