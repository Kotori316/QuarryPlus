package com.yogpc.qp.machines.quarry;

import java.util.Map;
import java.util.stream.Collectors;

import com.yogpc.qp.QuarryPlusTest;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.util.registry.Registry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnchantmentRestrictionTest extends QuarryPlusTest {
    @Test
    void instance() {
        var expected = new EnchantmentRestriction(Map.of(Enchantments.EFFICIENCY, 3, Enchantments.UNBREAKING, 1, Enchantments.SHARPNESS, Enchantments.SHARPNESS.getMaxLevel()));
        EnchantmentRestriction fromBuilder = EnchantmentRestriction.builder()
            .add(Enchantments.EFFICIENCY, 3)
            .add(Enchantments.SHARPNESS)
            .add(Enchantments.UNBREAKING, 1)
            .build();
        assertEquals(expected, fromBuilder);
    }

    @Test
    void emptyInstance() {
        var expected = new EnchantmentRestriction(Map.of());
        var restriction = EnchantmentRestriction.builder().build();
        assertEquals(expected, restriction);
    }

    @Test
    void ignoreAll() {
        var restriction = new EnchantmentRestriction(Map.of());
        var map = Registry.ENCHANTMENT.stream().map(e -> Map.entry(e, e.getMaxLevel()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        var result = restriction.filterMap(map);
        assertTrue(result.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("com.yogpc.qp.machines.EnchantmentLevelTest#enchantments")
    void limitOneEnchantment(Enchantment enchantment) {
        EnchantmentRestriction restriction = EnchantmentRestriction.builder().add(enchantment, 2).build();
        assertTrue(restriction.test(enchantment, 1));
        assertTrue(restriction.test(enchantment, 2));
        assertFalse(restriction.test(enchantment, 3));
        var map1 = Map.of(enchantment, 1);
        assertEquals(map1, restriction.filterMap(map1));
        var map2 = Map.of(enchantment, 2);
        assertEquals(map2, restriction.filterMap(map2));
        var map4 = Map.of(enchantment, 4);
        assertEquals(Map.of(), restriction.filterMap(map4));
    }

    @Test
    void limitLevel1() {
        EnchantmentRestriction fromBuilder = EnchantmentRestriction.builder()
            .add(Enchantments.EFFICIENCY)
            .add(Enchantments.SHARPNESS)
            .add(Enchantments.UNBREAKING)
            .build();
        var map = Registry.ENCHANTMENT.stream().map(e -> Map.entry(e, e.getMaxLevel()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        var result = fromBuilder.filterMap(map);
        assertEquals(Map.of(Enchantments.EFFICIENCY, 5, Enchantments.UNBREAKING, 3, Enchantments.SHARPNESS, Enchantments.SHARPNESS.getMaxLevel()),
            result);
    }

    @Test
    void limitLevel2() {
        EnchantmentRestriction fromBuilder = EnchantmentRestriction.builder()
            .add(Enchantments.EFFICIENCY, 3)
            .add(Enchantments.SHARPNESS)
            .add(Enchantments.UNBREAKING, 1)
            .build();
        var map = Registry.ENCHANTMENT.stream().map(e -> Map.entry(e, 1))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        var result = fromBuilder.filterMap(map);
        assertEquals(Map.of(Enchantments.EFFICIENCY, 1, Enchantments.UNBREAKING, 1, Enchantments.SHARPNESS, 1),
            result);
    }
}
