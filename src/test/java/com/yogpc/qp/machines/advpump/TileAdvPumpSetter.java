package com.yogpc.qp.machines.advpump;

import java.util.Map;

import net.minecraft.world.item.enchantment.Enchantment;

public final class TileAdvPumpSetter {
    public static void setEnchantment(TileAdvPump advPump, Map<Enchantment, Integer> enchantments) {
        var e = EnchantmentEfficiency.fromMap(enchantments);
        advPump.setEnchantment(e);
    }
}
