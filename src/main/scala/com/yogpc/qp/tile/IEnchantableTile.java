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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.yogpc.qp.gui.TranslationKeys;
import com.yogpc.qp.version.VersionUtil;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.commons.lang3.tuple.Pair;

import static jp.t2v.lab.syntax.MapStreamSyntax.byKey;
import static jp.t2v.lab.syntax.MapStreamSyntax.entry;
import static jp.t2v.lab.syntax.MapStreamSyntax.keys;
import static jp.t2v.lab.syntax.MapStreamSyntax.toAny;

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
    Map<Integer, Integer> getEnchantments();

    /**
     * @param id  Enchantment id
     * @param val level
     */
    void setEnchantent(short id, short val);

    default void sendEnchantMassage(EntityPlayer player) {
        Util.getEnchantmentsChat(this).forEach(c -> VersionUtil.sendMessage(player, c));
    }

    //Move static methods to this inner class because static method of an interface is not supported in Scala 2.11.1.
    public static class Util {
        public static void init(@Nonnull final IEnchantableTile te, @Nullable final NBTTagList tagList) {
            VersionUtil.nbtListStream(tagList).map(nbt -> Pair.of(nbt.getShort("id"), nbt.getShort("lvl")))
                .forEach(pair -> te.setEnchantent(pair.getKey(), pair.getValue()));
            te.G_reinit();
        }

        public static List<ITextComponent> getEnchantmentsChat(@Nonnull final IEnchantableTile te) {
            final Map<Integer, Integer> enchs = te.getEnchantments();
            if (enchs.size() <= 0) {
                return Collections.singletonList(new TextComponentTranslation(TranslationKeys.PLUSENCHANTNO));
            } else {
                LinkedList<ITextComponent> collect = enchs.entrySet().stream()
                    .map(keys(Enchantment::getEnchantmentByID))
                    .filter(byKey(APacketTile.nonNull)).map(toAny((enchantment, level) ->
                        new TextComponentTranslation(TranslationKeys.INDENT, new TextComponentTranslation(enchantment.getName()),
                            enchantment.getMaxLevel() != 1
                                ? new TextComponentTranslation(TranslationKeys.ENCHANT_LEVELS.getOrDefault(level, level.toString()))
                                : ""))).collect(Collectors.toCollection(LinkedList::new));
                collect.addFirst(new TextComponentTranslation(TranslationKeys.PLUSENCHANT));
                return collect;
            }
        }

        public static void enchantmentToIS(@Nonnull final IEnchantableTile te, @Nonnull final ItemStack is) {
            te.getEnchantments().entrySet().stream()
                .map(keys(Enchantment::getEnchantmentByID))
                .filter(byKey(APacketTile.nonNull))
                .forEach(entry(is::addEnchantment));
        }
    }

}
