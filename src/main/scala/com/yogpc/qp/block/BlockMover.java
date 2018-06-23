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

import cofh.api.block.IDismantleable;
import com.yogpc.qp.Config;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.QuarryPlusI;
import com.yogpc.qp.compat.InvUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import scala.Symbol;

@Optional.Interface(iface = "cofh.api.block.IDismantleable", modid = QuarryPlus.Optionals.COFH_block)
public class BlockMover extends Block implements IDismantleable {
    public static final Symbol SYMBOL = Symbol.apply("EnchantMover");
    public final ItemBlock itemBlock;

    public BlockMover() {
        super(Material.IRON);
        setHardness(1.2F);
        setCreativeTab(QuarryPlusI.creativeTab());
        setUnlocalizedName(QuarryPlus.Names.mover);
        setRegistryName(QuarryPlus.modID, QuarryPlus.Names.mover);
        itemBlock = new ItemBlock(this);
        itemBlock.setRegistryName(QuarryPlus.modID, QuarryPlus.Names.mover);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                    EnumHand hand, ItemStack s, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (InvUtils.isDebugItem(playerIn, hand)) return true;
        if (!playerIn.isSneaking() && !Config.content().disableMapJ().get(SYMBOL)) {
            playerIn.openGui(QuarryPlus.instance(), QuarryPlusI.guiIdMover(), worldIn, pos.getX(), pos.getY(), pos.getZ());
            return true;
        }
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, s, facing, hitX, hitY, hitZ);
    }

    @Override
    @Optional.Method(modid = QuarryPlus.Optionals.COFH_block)
    public ArrayList<ItemStack> dismantleBlock(World world, BlockPos pos, IBlockState state, EntityPlayer player, boolean returnDrops) {
        return ADismCBlock.dismantle(world, pos, state, returnDrops);
    }

    @Override
    @Optional.Method(modid = QuarryPlus.Optionals.COFH_block)
    public boolean canDismantle(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return true;
    }
}
