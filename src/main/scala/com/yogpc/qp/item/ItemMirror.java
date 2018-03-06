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
package com.yogpc.qp.item;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.QuarryPlusI;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemMirror extends ItemFood {

    public static final int Dimension_Meta = 1;
    public static final int Overworld_Meta = 2;

    public ItemMirror() {
        super(0, 0, false);
        setHasSubtypes(true);
        setUnlocalizedName(QuarryPlus.Names.mirror);
        setRegistryName(QuarryPlus.modID, QuarryPlus.Names.mirror);
        setCreativeTab(QuarryPlusI.creativeTab());
        setAlwaysEdible();
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {
        if (player instanceof EntityPlayerMP) {
            if (stack.getItemDamage() == Overworld_Meta) {
                if (player.dimension != 0) {
                    player.changeDimension(0);
                }
            } else if (!player.world.provider.canRespawnHere()) {
                if (stack.getItemDamage() == Dimension_Meta) {
                    player.changeDimension(player.world.provider.getRespawnDimension((EntityPlayerMP) player));
                } else {
                    return;
                }
            }
            BlockPos c = player.getBedLocation(player.dimension);
            if (c != null)
                c = EntityPlayer.getBedSpawnLocation(player.world, c, player.isSpawnForced(player.dimension));
            if (c == null)
                c = player.world.provider.getRandomizedSpawnPoint();
            player.setPositionAndUpdate(c.getX() + 0.5D, c.getY() + 0.1D, c.getZ() + 0.5D);
        }
    }

    @Override
    public int getMaxItemUseDuration(final ItemStack i) {
        return 100;
    }

    @Override
    public EnumAction getItemUseAction(final ItemStack i) {
        return EnumAction.EAT;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        playerIn.setActiveHand(handIn);
        return ActionResult.newResult(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
    }

    @Override
    public String getUnlocalizedName(final ItemStack is) {
        switch (is.getItemDamage()) {
            case Overworld_Meta:
                return "item.overworldmirror";
            case Dimension_Meta:
                return "item.dimensionmirror";
        }
        return super.getUnlocalizedName(is);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (isInCreativeTab(tab)) {
            items.add(new ItemStack(this, 1, 0));
            items.add(new ItemStack(this, 1, Dimension_Meta));
            items.add(new ItemStack(this, 1, Overworld_Meta));
        }
    }

}
