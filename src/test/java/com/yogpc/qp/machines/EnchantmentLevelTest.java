package com.yogpc.qp.machines;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.yogpc.qp.QuarryPlusTest;
import net.minecraft.core.Registry;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnchantmentLevelTest extends QuarryPlusTest {
    @ParameterizedTest
    @MethodSource("enchantments")
    void sortSameEnchantment(Enchantment enchantment) {
        var list = IntStream.rangeClosed(1, 5).mapToObj(i -> new EnchantmentLevel(enchantment, i)).toList();
        var sorted = IntStream.of(1, 5, 4, 2, 3).mapToObj(i -> new EnchantmentLevel(enchantment, i)).sorted(EnchantmentLevel.COMPARATOR).toList();
        assertIterableEquals(list, sorted);
    }

    @ParameterizedTest
    @MethodSource("enchantments")
    void fromId(Enchantment enchantment) {
        var id = Registry.ENCHANTMENT.getKey(enchantment);
        var ans = new EnchantmentLevel(enchantment, 1);
        var fromName = new EnchantmentLevel(id, 1);
        assertEquals(ans, fromName);
        assertEquals(id, fromName.enchantmentID());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 10})
    void sortSameLevel1(int level) {
        var ans = Stream.of(Enchantments.ALL_DAMAGE_PROTECTION, Enchantments.BLOCK_EFFICIENCY, Enchantments.SILK_TOUCH, Enchantments.UNBREAKING, Enchantments.BLOCK_FORTUNE, Enchantments.MENDING)
            .map(e -> new EnchantmentLevel(e, level)).toList();
        var shuffled = new ArrayList<>(ans);
        Collections.shuffle(shuffled, new Random(865));
        var sorted = shuffled.stream().sorted(EnchantmentLevel.COMPARATOR).toList();
        assertNotEquals(ans, shuffled);
        assertIterableEquals(ans, sorted);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 10})
    void sortSameLevel2(int level) {
        var ans = enchantments()
            .map(e -> new EnchantmentLevel(e, level)).toList();
        var shuffled = new ArrayList<>(ans);
        Collections.shuffle(shuffled, new Random(865));
        var sorted = shuffled.stream().sorted(EnchantmentLevel.COMPARATOR).toList();
        assertNotEquals(ans, shuffled);
        assertIterableEquals(ans, sorted);
    }

    @Test
    void hasEnchantmentTest1() {
        EnchantmentLevel.HasEnchantments enchantments = () -> List.of(new EnchantmentLevel(Enchantments.UNBREAKING, 4), new EnchantmentLevel(Enchantments.SILK_TOUCH, 1));
        assertAll(
            () -> assertEquals(1, enchantments.silktouchLevel()),
            () -> assertEquals(0, enchantments.fortuneLevel()),
            () -> assertEquals(4, enchantments.unbreakingLevel())
        );
    }

    @Test
    void hasEnchantmentTest2() {
        EnchantmentLevel.HasEnchantments enchantments = () -> List.of(new EnchantmentLevel(Enchantments.SILK_TOUCH, 1), new EnchantmentLevel(Enchantments.UNBREAKING, 4));
        assertAll(
            () -> assertEquals(0, enchantments.fortuneLevel()),
            () -> assertEquals(1, enchantments.silktouchLevel()),
            () -> assertEquals(4, enchantments.unbreakingLevel())
        );
    }

    static Stream<Enchantment> enchantments() {
        return Registry.ENCHANTMENT.stream();
    }

    @Test
    void dummy() {
        assertTrue(enchantments().findAny().isPresent());
    }
}
