package com.yogpc.qp.machine;

import com.yogpc.qp.enchantment.QuarryPickaxeEnchantment;
import com.yogpc.qp.gametest.GameTestFunctions;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public final class EnchantmentCacheTest {
    private static ItemEnchantments with(Map<ResourceKey<Enchantment>, Integer> keys, GameTestHelper helper) {
        var builder = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        for (Map.Entry<ResourceKey<Enchantment>, Integer> resourceKeyIntegerEntry : keys.entrySet()) {
            builder.set(GameTestFunctions.getEnchantment(helper, resourceKeyIntegerEntry.getKey()), resourceKeyIntegerEntry.getValue());
        }
        return builder.toImmutable();
    }

    public static void instance(GameTestHelper helper) {
        assertDoesNotThrow(EnchantmentCache::new);
        helper.succeed();
    }

    public static void getEnchantment(GameTestHelper helper) {
        var e = new EnchantmentCache();
        assertTrue(e.cache.isEmpty());
        var enchantments = with(Map.of(
            Enchantments.EFFICIENCY, 3,
            Enchantments.UNBREAKING, 1
        ), helper);

        var lookup = helper.getLevel().registryAccess().asGetterLookup();
        assertEquals(3, e.getLevel(enchantments, Enchantments.EFFICIENCY, lookup));
        assertFalse(e.cache.isEmpty());
        assertTrue(e.cache.containsKey(Enchantments.EFFICIENCY));
        assertFalse(e.cache.containsKey(Enchantments.UNBREAKING));
        assertEquals(1, e.getLevel(enchantments, Enchantments.UNBREAKING, lookup));
        assertTrue(e.cache.containsKey(Enchantments.UNBREAKING));
        assertEquals(0, e.getLevel(enchantments, Enchantments.FORTUNE, lookup));

        helper.succeed();
    }

    public static void getPickaxeEnchantment(GameTestHelper helper) {
        var e = new EnchantmentCache();
        assertNull(e.enchantmentsForPickaxe);
        var enchantments = with(Map.of(
            Enchantments.EFFICIENCY, 3,
            Enchantments.UNBREAKING, 1
        ), helper);
        var lookup = helper.getLevel().registryAccess().asGetterLookup();
        var p = e.getEnchantmentsForPickaxe(enchantments, lookup);
        assertEquals(p, e.enchantmentsForPickaxe);
        assertEquals(3, p.getLevel(GameTestFunctions.getEnchantment(helper, Enchantments.EFFICIENCY)));
        assertEquals(1, p.getLevel(GameTestFunctions.getEnchantment(helper, Enchantments.UNBREAKING)));
        assertEquals(0, p.getLevel(GameTestFunctions.getEnchantment(helper, Enchantments.FORTUNE)));
        assertEquals(1, p.getLevel(GameTestFunctions.getEnchantment(helper, QuarryPickaxeEnchantment.KEY)));

        helper.succeed();
    }
}
