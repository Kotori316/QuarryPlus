package com.yogpc.qp.gametest;

import com.yogpc.qp.enchantment.QuarryPickaxeEnchantment;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantment;

import static org.junit.jupiter.api.Assertions.*;

public final class EnchantmentTest {
    public static void findEnchantment(GameTestHelper helper) {
        var enchantment = assertDoesNotThrow(() -> GameTestFunctions.getEnchantment(helper, QuarryPickaxeEnchantment.KEY));
        assertInstanceOf(Enchantment.class, enchantment.value());
        helper.succeed();
    }

    public static void enchantmentTag(GameTestHelper helper) {
        var enchantment = GameTestFunctions.getEnchantment(helper, QuarryPickaxeEnchantment.KEY);
        assertTrue(enchantment.is(EnchantmentTags.PREVENTS_INFESTED_SPAWNS));
        helper.succeed();
    }
}
