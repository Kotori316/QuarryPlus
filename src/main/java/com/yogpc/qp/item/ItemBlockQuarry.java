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

import com.yogpc.qp.block.BlockQuarry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlockQuarry extends ItemBlock implements IEnchantableItem {

    public ItemBlockQuarry(final BlockQuarry b) {
        super(b);
    }

    @Override
    public boolean canMove(final ItemStack is, Enchantment enchantment) {
        return enchantment == Enchantments.SILK_TOUCH || enchantment == Enchantments.FORTUNE
                || enchantment == Enchantments.UNBREAKING || enchantment == Enchantments.EFFICIENCY;
    }

    @Override
    public boolean isBookEnchantable(final ItemStack itemstack1, final ItemStack itemstack2) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> subItems) {
        super.getSubItems(itemIn, tab, subItems);
        if ((Boolean) Launch.blackboard.getOrDefault("fml.deobfuscatedEnvironment", Boolean.FALSE)) {
            ItemStack stack = new ItemStack(Items.DIAMOND_PICKAXE);
            stack.addEnchantment(Enchantments.EFFICIENCY, 5);
            stack.addEnchantment(Enchantments.UNBREAKING, 3);
            {
                ItemStack stack1 = stack.copy();
                stack1.addEnchantment(Enchantments.FORTUNE, 3);
                subItems.add(stack1);
            }
            {
                ItemStack stack1 = stack.copy();
                stack1.addEnchantment(Enchantments.SILK_TOUCH, 1);
                subItems.add(stack1);
            }
        }
    }
}
