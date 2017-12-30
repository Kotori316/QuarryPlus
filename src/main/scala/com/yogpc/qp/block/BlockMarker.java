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

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.QuarryPlusI;
import com.yogpc.qp.compat.InvUtils;
import com.yogpc.qp.tile.TileMarker;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static com.yogpc.qp.block.ADismCBlock.FACING;

public class BlockMarker extends Block implements ITileEntityProvider {//BlockContainer {
    protected static final AxisAlignedBB STANDING_AABB = new AxisAlignedBB(.35, 0, .35, .65, .65, .65);
    protected static final AxisAlignedBB DOWN_AABB = new AxisAlignedBB(.35, .35, .35, .65, 1, .65);
    protected static final AxisAlignedBB NORTH_AABB = new AxisAlignedBB(.35, .35, .35, .65, .65, 1);
    protected static final AxisAlignedBB SOUTH_AABB = new AxisAlignedBB(.35, .35, 0, .65, .65, .65);
    protected static final AxisAlignedBB WEST_AABB = new AxisAlignedBB(.35, .35, .35, 1, .65, .65);
    protected static final AxisAlignedBB EAST_AABB = new AxisAlignedBB(0.0D, .35, .35, .65, .65, .65);
    public final ItemBlock itemBlock;

    public BlockMarker() {
        super(Material.CIRCUITS);
        setLightLevel(0.5F);
        setUnlocalizedName(QuarryPlus.Names.marker);
        setRegistryName(QuarryPlus.modID, QuarryPlus.Names.marker);
        setCreativeTab(QuarryPlusI.creativeTab());
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.UP));
        this.isBlockContainer = true;
        itemBlock = new ItemBlock(this);
        itemBlock.setRegistryName(QuarryPlus.modID, QuarryPlus.Names.marker);
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
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    @SuppressWarnings("deprecation")
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return true;
    }

    @Nullable
    @Override
    @SuppressWarnings("deprecation")
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, World worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    @SuppressWarnings("deprecation")
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        switch (state.getValue(FACING)) {
            case UP:
                return STANDING_AABB;
            case DOWN:
                return DOWN_AABB;
            case EAST:
                return EAST_AABB;
            case WEST:
                return WEST_AABB;
            case NORTH:
                return NORTH_AABB;
            case SOUTH:
                return SOUTH_AABB;
        }
        return STANDING_AABB;
    }


    @Override
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side) {
        return canPlaceAt(worldIn, pos, side) || canPlaceAt(worldIn, pos, EnumFacing.UP);
    }

    private boolean canPlaceAt(World worldIn, BlockPos pos, EnumFacing facing) {
        BlockPos blockpos = pos.offset(facing.getOpposite());
        return worldIn.isSideSolid(blockpos, facing, true);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        if (!worldIn.isRemote) {
            TileEntity entity = worldIn.getTileEntity(pos);
            if (TileMarker.class.isInstance(entity)) {
                TileMarker marker = (TileMarker) entity;
                if (marker.link != null) {
                    marker.link.removeConnection(false);
                }
            }
        }
        worldIn.removeTileEntity(pos);
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing,
                                            float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, ItemStack hand) {
        if (canPlaceAt(world, pos, facing))
            return getDefaultState().withProperty(FACING, facing);
        return getDefaultState().withProperty(FACING, EnumFacing.UP);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.getFront(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex();
    }

    @Override
    @SuppressWarnings({"deprecation"})
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn/*, BlockPos fromPos*/) {
        Optional.ofNullable(((TileMarker) worldIn.getTileEntity(pos))).ifPresent(TileMarker::G_updateSignal);
        dropTorchIfCantStay(state, worldIn, pos);
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        super.onBlockAdded(worldIn, pos, state);
        dropTorchIfCantStay(state, worldIn, pos);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        Optional.ofNullable(((TileMarker) worldIn.getTileEntity(pos))).ifPresent(TileMarker::requestTicket);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                    EnumHand hand, ItemStack s, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (InvUtils.isDebugItem(playerIn, hand)) return true;
        if (!worldIn.isRemote && s != null) {
            Item item = playerIn.getHeldItem(hand).getItem();
            TileMarker marker = (TileMarker) worldIn.getTileEntity(pos);
            if (marker != null) {
                if (item == QuarryPlusI.itemTool() && playerIn.getHeldItem(hand).getItemDamage() == 0) {
                    final TileMarker.Link l = marker.link;
                    if (l == null)
                        return true;
                    playerIn.addChatComponentMessage(new TextComponentTranslation("chat.markerarea"));
                    String sb = "x:" + l.xn + " y:" + l.yn + " z:" + l.zn + " - x:" + l.xx + " y:" + l.yx + " z:" + l.zx;
                    playerIn.addChatComponentMessage(new TextComponentString(sb));// NP coord info
                    return true;
                } else {
                    marker.S_tryConnection();
                }
            }
        }
        return true;
    }

    @Override
    public TileMarker createNewTileEntity(final World w, final int m) {
        return new TileMarker();
    }

    private void dropTorchIfCantStay(IBlockState state, World worldIn, BlockPos pos) {
        if (state.getBlock() != this || !this.canPlaceAt(worldIn, pos, state.getValue(FACING))) {
            this.dropBlockAsItem(worldIn, pos, state, 0);
            worldIn.setBlockToAir(pos);
        }
    }
}
