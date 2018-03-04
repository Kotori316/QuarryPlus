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

package com.yogpc.qp.tile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public interface IEnchantableTile {

    int FortuneID = Enchantment.getEnchantmentID(Enchantments.FORTUNE);
    int SilktouchID = Enchantment.getEnchantmentID(Enchantments.SILK_TOUCH);
    int EfficiencyID = Enchantment.getEnchantmentID(Enchantments.EFFICIENCY);
    int UnbreakingID = Enchantment.getEnchantmentID(Enchantments.UNBREAKING);

    void G_reinit();

    /**
     * @return Map (Enchantment id, level)
     */
    @Nonnull
    Map<Integer, Byte> getEnchantments();

    /**
     * @param id  Enchantment id
     * @param val level
     */
    void setEnchantent(short id, short val);

    default void sendEnchantMassage(EntityPlayer player) {
        getEnchantmentsChat(this).forEach(player::sendMessage);
    }

    public static void init(@Nonnull final IEnchantableTile te, @Nullable final NBTTagList tagList) {
        if (tagList != null)
            for (int i = 0; i < tagList.tagCount(); i++)
                te.setEnchantent(tagList.getCompoundTagAt(i).getShort("id"), tagList.getCompoundTagAt(i).getShort("lvl"));
        te.G_reinit();
    }

    public static List<ITextComponent> getEnchantmentsChat(@Nonnull final IEnchantableTile te) {
        final List<ITextComponent> als = new ArrayList<>();
        final Map<Integer, Byte> enchs = te.getEnchantments();
        if (enchs.size() <= 0)
            als.add(new TextComponentTranslation("chat.plusenchantno"));
        else
            als.add(new TextComponentTranslation("chat.plusenchant"));
        for (final Map.Entry<Integer, Byte> e : enchs.entrySet()) {
            Enchantment enchantment = Enchantment.getEnchantmentByID(e.getKey());
            if (enchantment != null) {
                als.add(new TextComponentTranslation("chat.indent", new TextComponentTranslation(enchantment.getName()),
                        enchantment.getMaxLevel() != 1 ? new TextComponentTranslation("enchantment.level." + e.getValue()) : ""));
            }
        }
        return als;
    }

    public static void enchantmentToIS(@Nonnull final IEnchantableTile te, @Nonnull final ItemStack is) {
        final Map<Integer, Byte> enchs = te.getEnchantments();
        for (final Map.Entry<Integer, Byte> e : enchs.entrySet()) {
            Enchantment ench = Enchantment.getEnchantmentByID(e.getKey());
            if (ench != null) {
                is.addEnchantment(ench, e.getValue());
            }
        }
    }

}
