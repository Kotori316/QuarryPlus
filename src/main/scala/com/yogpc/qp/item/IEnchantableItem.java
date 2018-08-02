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

import java.util.function.Predicate;

import javax.annotation.Nonnull;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;

public interface IEnchantableItem {

    /**
     * You should not think max enchantment level in this method
     *
     * @param is          target ItemStack. It is never null.
     * @param enchantment target enchantment
     * @return that ItemStack can move enchantment on EnchantMover
     */
    boolean canMove(@Nonnull ItemStack is, Enchantment enchantment);

    Predicate<Enchantment> FALSE = o -> false;
    Predicate<Enchantment> SILKTOUCH = enchantment -> enchantment == Enchantments.SILK_TOUCH;
    Predicate<Enchantment> FORTUNE = enchantment -> enchantment == Enchantments.FORTUNE;
    Predicate<Enchantment> EFFICIENCY = enchantment -> enchantment == Enchantments.EFFICIENCY;
    Predicate<Enchantment> UNBREAKING = enchantment -> enchantment == Enchantments.UNBREAKING;
}
