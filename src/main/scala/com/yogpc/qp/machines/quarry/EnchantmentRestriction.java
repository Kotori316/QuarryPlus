package com.yogpc.qp.machines.quarry;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import net.minecraft.enchantment.Enchantment;

record EnchantmentRestriction(Map<Enchantment, Integer> restrictionMap)
    implements BiPredicate<Enchantment, Integer> {

    EnchantmentRestriction(Map<Enchantment, Integer> restrictionMap) {
        this.restrictionMap = Map.copyOf(restrictionMap);
    }

    @Override
    public boolean test(Enchantment enchantment, Integer level) {
        return restrictionMap.getOrDefault(enchantment, 0) >= level;
    }

    Map<Enchantment, Integer> filterMap(Map<Enchantment, Integer> stackEnchantments) {
        return stackEnchantments.entrySet().stream()
            .filter(e -> test(e.getKey(), e.getValue()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    static Builder builder() {
        return new Builder();
    }

    static class Builder {
        private final Map<Enchantment, Integer> map = new HashMap<>();

        private Builder() {
        }

        Builder add(Enchantment enchantment) {
            return add(enchantment, enchantment.getMaxLevel());
        }

        Builder add(Enchantment enchantment, int maxLevel) {
            map.put(enchantment, Math.max(maxLevel, 0));
            return this;
        }

        EnchantmentRestriction build() {
            return new EnchantmentRestriction(map);
        }
    }
}
