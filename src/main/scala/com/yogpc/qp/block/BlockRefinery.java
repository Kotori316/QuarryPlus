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
import com.yogpc.qp.item.ItemBlockRefinery;
import com.yogpc.qp.tile.IEnchantableTile;
import com.yogpc.qp.tile.TileRefinery;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

//import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class BlockRefinery extends ADismCBlock {

    public BlockRefinery() {
        super(Material.ANVIL, QuarryPlus.Names.refinery, ItemBlockRefinery::new);
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
            IEnchantableTile.Util.enchantmentToIS(tile, is);
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
            Optional.ofNullable((IEnchantableTile) worldIn.getTileEntity(pos)).ifPresent(t -> IEnchantableTile.Util.init(t, stack.getEnchantmentTagList()));
        }
    }

   /* @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing,
                                            float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    private static void fill(TileRefinery refinery, EntityPlayer player, EnumHand hand, EnumFacing facing) {
        ItemStack current = player.getHeldItem(hand);
        IFluidHandler handler = refinery.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing);
        IFluidHandlerItem handlerItem = FluidUtil.getFluidHandler(current);
        if (handlerItem != null && handler != null) {
            int fill = handler.fill(FluidUtil.getFluidContained(current), false);
            if (fill > 0) {
                handler.fill(handlerItem.drain(fill, !player.capabilities.isCreativeMode), true);
                player.setHeldItem(hand, handlerItem.getContainer());
            }
        }
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = playerIn.getHeldItem(hand);
        if (InvUtils.isDebugItem(playerIn, hand)) return true;
        if (BuildcraftHelper.isWrench(playerIn, hand, stack, new RayTraceResult(new Vec3d(hitX, hitY, hitZ), facing, pos))) {
            worldIn.setBlockState(pos, state.withProperty(FACING, state.getValue(FACING).rotateYCCW()));
            return true;
        }
        Optional<TileRefinery> tileEntity = Optional.ofNullable((TileRefinery) worldIn.getTileEntity(pos));
        Consumer<TileRefinery> sendPacket = t -> PacketHandler.sendToAround(TileMessage.create(t), worldIn, pos);
        if (stack.getItem() == QuarryPlusI.itemTool() && stack.getItemDamage() == 0) {
            if (!worldIn.isRemote) {
                Consumer<TileRefinery> consumer1 = t -> t.sendEnchantMassage(playerIn);
                tileEntity.ifPresent(consumer1.andThen(sendPacket));
            }
            return true;
        } else if (FluidUtil.getFluidHandler(stack) != null) {
            if (!worldIn.isRemote) {
                Consumer<TileRefinery> consumer1 = refinery -> fill(refinery, playerIn, hand, facing);
                tileEntity.ifPresent(consumer1.andThen(sendPacket));
            }
            return true;
        } else if (!worldIn.isRemote) {
            tileEntity.ifPresent(sendPacket);
        }
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }*/

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
    @SuppressWarnings("deprecation")
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
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

    @Override
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
//        super.getSubBlocks(itemIn, tab, list);
    }

    @Override
    protected boolean canRotate() {
        return true;
    }
}
