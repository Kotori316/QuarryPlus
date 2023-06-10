package com.yogpc.qp.machines.quarry;

import com.yogpc.qp.utils.MapStreamSyntax;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;


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
                .filter(MapStreamSyntax.byEntry(this))
                .collect(MapStreamSyntax.entryToMap());
    }

    static Builder builder() {
        return new Builder();
    }

    static class Builder {
        private final Map<Enchantment, Integer> map = new HashMap<>();

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
