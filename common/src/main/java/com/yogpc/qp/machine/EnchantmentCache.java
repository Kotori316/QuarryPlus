package com.yogpc.qp.machine;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public final class EnchantmentCache {
    ItemEnchantments target;
    Object2IntMap<ResourceKey<Enchantment>> cache = new Object2IntOpenHashMap<>();

    public int getLevel(ItemEnchantments enchantments, ResourceKey<Enchantment> enchantment, HolderGetter.Provider lookup) {
        if (target != enchantments) {
            target = enchantments;
            cache.clear();
        }

        return cache.computeIfAbsent(enchantment, key -> {
            var e = lookup.lookup(Registries.ENCHANTMENT).flatMap(g -> g.get(enchantment));
            // Avoid auto boxing
            // noinspection OptionalIsPresent
            if (e.isEmpty()) {
                return 0;
            }
            return enchantments.getLevel(e.get());
        });
    }
}
