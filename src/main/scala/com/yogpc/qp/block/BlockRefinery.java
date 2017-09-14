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

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.compat.BuildCraftHelper;
import com.yogpc.qp.compat.EnchantmentHelper;
import com.yogpc.qp.compat.InvUtils;
import com.yogpc.qp.item.ItemTool;
import com.yogpc.qp.tile.IEnchantableTile;
import com.yogpc.qp.tile.TileRefinery;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class BlockRefinery extends ADismCBlock {

    public BlockRefinery() {
        super(Material.IRON, QuarryPlus.Names.refinery, ItemBlock::new);
        setHardness(5F);
        setDefaultState(getBlockState().getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public TileEntity createNewTileEntity(final World w, final int m) {
        return new TileRefinery();
    }

    private final ArrayList<ItemStack> drop = new ArrayList<>();

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        this.drop.clear();
        final TileRefinery tile = (TileRefinery) worldIn.getTileEntity(pos);
        if (worldIn.isRemote || tile == null)
            return;
        final int count = quantityDropped(state, 0, worldIn.rand);
        final Item it = getItemDropped(state, worldIn.rand, 0);
        for (int i = 0; i < count; i++) {
            final ItemStack is = new ItemStack(it, 1, damageDropped(state));
            EnchantmentHelper.enchantmentToIS(tile, is);
            this.drop.add(is);
        }

        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        return this.drop;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        if (!worldIn.isRemote) {
            EnumFacing facing = placer.getHorizontalFacing();
            worldIn.setBlockState(pos, state.withProperty(FACING, facing), 2);
            EnchantmentHelper.init((IEnchantableTile) worldIn.getTileEntity(pos), stack.getEnchantmentTagList());
        }
    }

    private static boolean fill(IFluidHandler handler, EntityPlayer player, EnumHand hand) {
        ItemStack current = player.getHeldItem(hand);
        IFluidHandlerItem handlerItem = FluidUtil.getFluidHandler(current);
        if (handlerItem != null) {
            int fill = handler.fill(FluidUtil.getFluidContained(current), false);
            if (fill > 0) {
                handler.fill(handlerItem.drain(fill, !player.capabilities.isCreativeMode), true);
                player.setHeldItem(hand, handlerItem.getContainer());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = playerIn.getHeldItem(hand);
        if (InvUtils.isDebugItem(playerIn, hand)) return true;
        if (BuildCraftHelper.isWrench(playerIn, hand, stack, new RayTraceResult(new Vec3d(hitX, hitY, hitZ), facing, pos))) {
            worldIn.setBlockState(pos, state.withProperty(FACING, state.getValue(FACING).rotateYCCW()));
            return true;
        }
        if (stack.getItem() instanceof ItemTool && stack.getItemDamage() == 0) {
            if (!worldIn.isRemote) {
                EnchantmentHelper.getEnchantmentsChat((IEnchantableTile) worldIn.getTileEntity(pos)).forEach(playerIn::sendMessage);
            }
            return true;
        } else if (!worldIn.isRemote) {
            if (fill((TileRefinery) worldIn.getTileEntity(pos), playerIn, hand))
                return true;
        }
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
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
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        EnumFacing facing = state.getValue(FACING);
        return facing.getIndex();
    }

    @SuppressWarnings("deprecation")
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.getFront(meta));
    }
}
