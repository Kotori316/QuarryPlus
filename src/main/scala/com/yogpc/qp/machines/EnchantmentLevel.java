package com.yogpc.qp.machines;

import java.util.List;
import java.util.Map;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

public record EnchantmentLevel(Enchantment enchantment, int level) {
    public EnchantmentLevel(Map.Entry<Enchantment, Integer> entry) {
        this(entry.getKey(), entry.getValue());
    }

    @Nullable
    public Identifier enchantmentID() {
        return Registry.ENCHANTMENT.getId(enchantment());
    }

    public interface HasEnchantments {
        List<EnchantmentLevel> getEnchantments();
    }
}
