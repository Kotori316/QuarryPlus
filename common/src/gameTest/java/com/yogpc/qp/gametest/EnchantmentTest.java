package com.yogpc.qp.gametest;

import com.yogpc.qp.enchantment.QuarryPickaxeEnchantment;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.enchantment.Enchantment;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public final class EnchantmentTest {
    public static void findEnchantment(GameTestHelper helper) {
        var enchantment = assertDoesNotThrow(() -> helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getOrThrow(QuarryPickaxeEnchantment.KEY));
        assertInstanceOf(Enchantment.class, enchantment);
        helper.succeed();
    }
}
