package com.yogpc.qp.machines;

import net.minecraft.world.item.enchantment.Enchantments;

public record EnchantmentHolder(int efficiency, int unbreaking, int fortune, int silktouch) {
    public static EnchantmentHolder makeHolder(EnchantmentLevel.HasEnchantments enchantments) {
        return new EnchantmentHolder(enchantments.getLevel(Enchantments.BLOCK_EFFICIENCY),
            enchantments.getLevel(Enchantments.UNBREAKING),
            enchantments.getLevel(Enchantments.BLOCK_FORTUNE),
            enchantments.getLevel(Enchantments.SILK_TOUCH));
    }
}
