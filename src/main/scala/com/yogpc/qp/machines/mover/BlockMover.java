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
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import scala.Symbol;

//@Optional.InterfaceList({
//    @Optional.Interface(iface = "cofh.api.block.IDismantleable", modid = QuarryPlus.Optionals.COFH_modID),
//    @Optional.Interface(iface = "ic2.api.tile.IWrenchable", modid = QuarryPlus.Optionals.IC2_modID)})
public class BlockMover extends Block implements IDisabled /*IDismantleable, IWrenchable*/ {
    public static final Symbol SYMBOL = Symbol.apply("EnchantMover");
    public static final String GUI_ID = QuarryPlus.modID + ":gui_" + QuarryPlus.Names.mover;
    public final ItemBlock itemBlock;

    public BlockMover() {
        super(Properties.create(Material.IRON).hardnessAndResistance(1.2f));
        setRegistryName(QuarryPlus.modID, QuarryPlus.Names.mover);
        itemBlock = new ItemBlock(this, new Item.Properties().group(Holder.tab()));
        itemBlock.setRegistryName(QuarryPlus.modID, QuarryPlus.Names.mover);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean onBlockActivated(IBlockState state, World worldIn, BlockPos pos, EntityPlayer player,
                                    EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (InvUtils.isDebugItem(player, hand)) return true;
        if (player.isSneaking()
            && BuildcraftHelper.isWrench(player, hand, player.getHeldItem(hand), new RayTraceResult(new Vec3d(hitX, hitY, hitZ), side, pos))) {
            if (!worldIn.isRemote)
                QPBlock.dismantle(worldIn, pos, state, false);
            return true;
        }
        if (!player.isSneaking()) {
            if (!worldIn.isRemote) {
                NetworkHooks.openGui(((EntityPlayerMP) player), new InteractionObject(pos), pos);
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

    private static class InteractionObject implements IInteractionObject {
        private final BlockPos pos;

        public InteractionObject(BlockPos pos) {
            this.pos = pos;
        }

        @Override
        public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
            return new ContainerMover(playerInventory, playerIn.getEntityWorld(), pos);
        }

        @Override
        public String getGuiID() {
            return GUI_ID;
        }

        @Override
        public ITextComponent getName() {
            return new TextComponentTranslation(TranslationKeys.mover);
        }

        @Override
        public boolean hasCustomName() {
            return false;
        }

        @Nullable
        @Override
        public ITextComponent getCustomName() {
            return null;
        }
    }
}
