package com.yogpc.qp.machines;

import java.util.Set;
import java.util.function.Predicate;

import net.minecraft.world.item.enchantment.Enchantment;

public interface EnchantableItem extends Predicate<Enchantment> {
    Set<Enchantment> acceptEnchantments();

    @Override
    default boolean test(Enchantment enchantment) {
        return acceptEnchantments().contains(enchantment);
    }
}
