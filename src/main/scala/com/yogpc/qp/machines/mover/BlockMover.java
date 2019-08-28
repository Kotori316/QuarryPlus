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

package com.yogpc.qp.machines.mover;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.compat.BuildcraftHelper;
import com.yogpc.qp.compat.InvUtils;
import com.yogpc.qp.machines.TranslationKeys;
import com.yogpc.qp.machines.base.IDisabled;
import com.yogpc.qp.machines.base.QPBlock;
import com.yogpc.qp.utils.Holder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import scala.Symbol;

//@Optional.InterfaceList({
//    @Optional.Interface(iface = "cofh.api.block.IDismantleable", modid = QuarryPlus.Optionals.COFH_modID),
//    @Optional.Interface(iface = "ic2.api.tile.IWrenchable", modid = QuarryPlus.Optionals.IC2_modID)})
public class BlockMover extends Block implements IDisabled /*IDismantleable, IWrenchable*/ {
    public static final Symbol SYMBOL = Symbol.apply("EnchantMover");
    public static final String GUI_ID = QuarryPlus.modID + ":gui_" + QuarryPlus.Names.mover;
    public final BlockItem itemBlock;

    public BlockMover() {
        super(Properties.create(Material.IRON).hardnessAndResistance(1.2f));
        setRegistryName(QuarryPlus.modID, QuarryPlus.Names.mover);
        itemBlock = new BlockItem(this, new Item.Properties().group(Holder.tab()));
        itemBlock.setRegistryName(QuarryPlus.modID, QuarryPlus.Names.mover);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos,
                                    PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (InvUtils.isDebugItem(player, hand)) return true;
        if (player.isSneaking()
            && BuildcraftHelper.isWrench(player, hand, player.getHeldItem(hand), hit)) {
            if (!worldIn.isRemote)
                QPBlock.dismantle(worldIn, pos, state, false);
            return true;
        }
        if (!player.isSneaking()) {
            if (!worldIn.isRemote) {
                NetworkHooks.openGui(((ServerPlayerEntity) player), new InteractionObject(pos), pos);
            }
            return true;
        }
        return false;
    }

    @Override
    public Item asItem() {
        return itemBlock;
    }

    @Override
    public Symbol getSymbol() {
        return SYMBOL;
    }
/*
    @Override
    @Optional.Method(modid = QuarryPlus.Optionals.COFH_modID)
    public ArrayList<ItemStack> dismantleBlock(World world, BlockPos pos, IBlockState state, EntityPlayer player, boolean returnDrops) {
        return ADismCBlock.dismantle(world, pos, state, returnDrops);
    }

    @Override
    @Optional.Method(modid = QuarryPlus.Optionals.COFH_modID)
    public boolean canDismantle(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return true;
    }

    @Override
    @Optional.Method(modid = QuarryPlus.Optionals.IC2_modID)
    public EnumFacing getFacing(World world, BlockPos pos) {
        return EnumFacing.UP;
    }

    @Override
    @Optional.Method(modid = QuarryPlus.Optionals.IC2_modID)
    public boolean setFacing(World world, BlockPos pos, EnumFacing newDirection, EntityPlayer player) {
        return false;
    }

    @Override
    @Optional.Method(modid = QuarryPlus.Optionals.IC2_modID)
    public boolean wrenchCanRemove(World world, BlockPos pos, EntityPlayer player) {
        return true;
    }

    @Override
    @Optional.Method(modid = QuarryPlus.Optionals.IC2_modID)
    public List<ItemStack> getWrenchDrops(World world, BlockPos pos, IBlockState state, TileEntity te, EntityPlayer player, int fortune) {
        NonNullList<ItemStack> list = NonNullList.create();
        state.getBlock().getDrops(list, world, pos, state, fortune);
        return list;
    }*/

    private static class InteractionObject implements INamedContainerProvider {
        private final BlockPos pos;

        public InteractionObject(BlockPos pos) {
            this.pos = pos;
        }

        @Override
        public ITextComponent getDisplayName() {
            return new TranslationTextComponent(TranslationKeys.mover);
        }

        @Override
        public Container createMenu(int id, PlayerInventory inventory, PlayerEntity player) {
            return new ContainerMover(id, player, pos);
        }
    }
}
