package com.yogpc.qp.fabric;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.gametest.GameTestFunctions;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;

import static org.junit.jupiter.api.Assertions.assertTrue;

public final class EnchantmentTest {

    @GameTest()
    public void quarryEnchantmentEfficiency(GameTestHelper helper) {
        var enchantment = GameTestFunctions.getEnchantment(helper, Enchantments.EFFICIENCY);
        var stack = new ItemStack(PlatformAccess.getAccess().registerObjects().quarryBlock().get());
        assertTrue(enchantment.value().canEnchant(stack));
        helper.succeed();
    }

    @GameTest()
    public void quarryEnchantmentUnbreaking(GameTestHelper helper) {
        var enchantment = GameTestFunctions.getEnchantment(helper, Enchantments.UNBREAKING);
        var stack = new ItemStack(PlatformAccess.getAccess().registerObjects().quarryBlock().get());
        assertTrue(enchantment.value().canEnchant(stack));
        helper.succeed();
    }

    @GameTest()
    public void quarryEnchantmentFortune(GameTestHelper helper) {
        var enchantment = GameTestFunctions.getEnchantment(helper, Enchantments.FORTUNE);
        var stack = new ItemStack(PlatformAccess.getAccess().registerObjects().quarryBlock().get());
        assertTrue(enchantment.value().canEnchant(stack));
        helper.succeed();
    }
}
