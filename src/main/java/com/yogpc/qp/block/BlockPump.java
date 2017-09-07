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
import com.yogpc.qp.gui.GuiP_List;
import com.yogpc.qp.item.ItemBlockPump;
import com.yogpc.qp.item.ItemTool;
import com.yogpc.qp.tile.IEnchantableTile;
import com.yogpc.qp.tile.TilePump;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
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

public class BlockPump extends ADismCBlock {

    public static final PropertyBool CONNECTED = PropertyBool.create("connected");

    public BlockPump() {
        super(Material.IRON, QuarryPlus.Names.pump);
        setHardness(5F);
        setDefaultState(getBlockState().getBaseState().withProperty(ACTING, false).withProperty(CONNECTED, false));
        itemBlock = new ItemBlockPump(this);
        itemBlock.setRegistryName(QuarryPlus.modID, QuarryPlus.Names.pump);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, ACTING, CONNECTED);
    }

    @Override
    public TilePump createNewTileEntity(final World w, final int m) {
        return new TilePump();
    }

    private final ArrayList<ItemStack> drop = new ArrayList<>();

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        this.drop.clear();
        final TilePump tile = (TilePump) worldIn.getTileEntity(pos);
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
        EnchantmentHelper.init((IEnchantableTile) worldIn.getTileEntity(pos), stack.getEnchantmentTagList());
    }

    @SuppressWarnings({"deprecation", "ConstantConditions"})
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        ((TilePump) worldIn.getTileEntity(pos)).G_reinit();
    }

    @SuppressWarnings({"MethodCallSideOnly", "ConstantConditions", "NewExpressionSideOnly"})
    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = playerIn.getHeldItem(hand);
        if (BuildCraftHelper.isWrench(playerIn, hand, stack, new RayTraceResult(new Vec3d(hitX, hitY, hitZ), facing, pos))) {
            ((TilePump) worldIn.getTileEntity(pos)).S_changeRange(playerIn);
            return true;
        }
        if (stack.getItem() instanceof ItemTool) {
            if (!worldIn.isRemote && stack.getItemDamage() == 0) {
                TilePump pump = (TilePump) worldIn.getTileEntity(pos);
                EnchantmentHelper.getEnchantmentsChat(pump).forEach(playerIn::sendMessage);
                pump.C_getNames().forEach(playerIn::sendMessage);
            } else if (stack.getItemDamage() == 2) {
                TilePump pump = (TilePump) worldIn.getTileEntity(pos);
                if (!worldIn.isRemote) {
                    pump.S_OpenGUI(facing, playerIn);
                } else {
                    Minecraft.getMinecraft().displayGuiScreen(new GuiP_List((byte) facing.ordinal(), pump));
                }
            }
            return true;
        }
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        boolean work = state.getValue(ACTING);
        boolean connected = state.getValue(CONNECTED);
        return (work ? 1 : 0) & (connected ? 2 : 0);
    }

    @SuppressWarnings("deprecation")
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(ACTING, (meta & 1) == 1).withProperty(CONNECTED, (meta & 2) == 2);
    }
}
