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

package com.yogpc.qp.compat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.yogpc.qp.tile.IEnchantableTile;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

@SuppressWarnings("ConstantConditions")
public final class EnchantmentHelper {
    public static void init(final IEnchantableTile te, final NBTTagList nbttl) {
        if (nbttl != null)
            for (int i = 0; i < nbttl.tagCount(); i++)
                te.setEnchantent(nbttl.getCompoundTagAt(i).getShort("id"), nbttl.getCompoundTagAt(i).getShort("lvl"));
        te.G_reinit();
    }

    public static List<ITextComponent> getEnchantmentsChat(final IEnchantableTile te) {
        final List<ITextComponent> als = new ArrayList<>();
        final Map<Integer, Byte> enchs = te.getEnchantments();
        if (enchs.size() <= 0)
            als.add(new TextComponentTranslation("chat.plusenchantno"));
        else
            als.add(new TextComponentTranslation("chat.plusenchant"));
        for (final Map.Entry<Integer, Byte> e : enchs.entrySet()) {
            Enchantment enchantment = Enchantment.getEnchantmentByID(e.getKey());
            als.add(new TextComponentTranslation("chat.indent", new TextComponentTranslation(enchantment.getName()),
                    enchantment.getMaxLevel() != 1 ? new TextComponentTranslation("enchantment.level." + e.getValue()) : ""));
        }
        return als;
    }

    public static void enchantmentToIS(final IEnchantableTile te, final ItemStack is) {
        final Map<Integer, Byte> enchs = te.getEnchantments();
        for (final Map.Entry<Integer, Byte> e : enchs.entrySet())
            is.addEnchantment(Enchantment.getEnchantmentByID(e.getKey()), e.getValue());
    }
}
