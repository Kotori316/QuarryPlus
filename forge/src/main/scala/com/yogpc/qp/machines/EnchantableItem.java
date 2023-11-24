package com.yogpc.qp.machines;

import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Set;
import java.util.function.Predicate;

public interface EnchantableItem extends Predicate<Enchantment> {
    Set<Enchantment> acceptEnchantments();

    @Override
    default boolean test(Enchantment enchantment) {
        return acceptEnchantments().contains(enchantment);
    }
}
