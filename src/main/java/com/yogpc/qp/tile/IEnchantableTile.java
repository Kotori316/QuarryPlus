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

import java.util.Map;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Enchantments;

public interface IEnchantableTile {

    int FortuneID = Enchantment.getEnchantmentID(Enchantments.FORTUNE);
    int SilktouchID = Enchantment.getEnchantmentID(Enchantments.SILK_TOUCH);
    int EfficiencyID = Enchantment.getEnchantmentID(Enchantments.EFFICIENCY);
    int UnbreakingID = Enchantment.getEnchantmentID(Enchantments.UNBREAKING);

    void G_reinit();

    /**
     * @return Map (Enchantment id, level)
     */
    Map<Integer, Byte> getEnchantments();

    /**
     * @param id  Enchantment id
     * @param val level
     */
    void setEnchantent(short id, short val);
}
