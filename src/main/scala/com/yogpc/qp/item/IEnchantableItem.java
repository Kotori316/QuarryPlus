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
import jp.t2v.lab.syntax.MapStreamSyntax;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
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

    Predicate<Enchantment> FALSE = MapStreamSyntax.always_false();
    Predicate<Enchantment> SILKTOUCH = Predicate.isEqual(Enchantments.SILK_TOUCH);
    Predicate<Enchantment> FORTUNE = Predicate.isEqual(Enchantments.FORTUNE);
    Predicate<Enchantment> EFFICIENCY = Predicate.isEqual(Enchantments.EFFICIENCY);
    Predicate<Enchantment> UNBREAKING = Predicate.isEqual(Enchantments.UNBREAKING);

    default ItemStack[] stacks() {
        return new ItemStack[]{new ItemStack((Item) this, 1, 0)};
    }

    default boolean isValidInBookMover() {
        return true;
    }
}
