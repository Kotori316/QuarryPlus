package com.yogpc.qp.machines;

import com.yogpc.qp.QuarryPlusTest;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.registries.ForgeRegistries;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(QuarryPlusTest.class)
class EnchantmentLevelTest {
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
        var id = ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
        var ans = new EnchantmentLevel(enchantment, 1);
        var fromName = new EnchantmentLevel(id, 1);
        assertEquals(ans, fromName);
        assertEquals(id, fromName.enchantmentID());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 10})
    void sortSameLevel1(int level) {
        var ans = Stream.of(Enchantments.BLOCK_EFFICIENCY, Enchantments.BLOCK_FORTUNE, Enchantments.MENDING,
                Enchantments.ALL_DAMAGE_PROTECTION, Enchantments.SILK_TOUCH, Enchantments.UNBREAKING)
            .map(e -> new EnchantmentLevel(e, level)).toList();
        var shuffled = new ArrayList<>(ans);
        Collections.shuffle(shuffled, new Random(865));
        var sorted = shuffled.stream().sorted(EnchantmentLevel.COMPARATOR).toList();
        assertNotEquals(ans, shuffled);
        assertIterableEquals(ans, sorted, "Sorted=" + sorted);
    }

    @Test
    void hasEnchantmentTest1() {
        EnchantmentLevel.HasEnchantments enchantments = () -> List.of(new EnchantmentLevel(Enchantments.UNBREAKING, 4), new EnchantmentLevel(Enchantments.SILK_TOUCH, 1));
        assertAll(
            () -> assertEquals(0, enchantments.getLevel(Enchantments.BINDING_CURSE)),
            () -> assertEquals(0, enchantments.getLevel(Enchantments.BLOCK_EFFICIENCY)),
            () -> assertEquals(0, enchantments.efficiencyLevel()),
            () -> assertEquals(1, enchantments.silktouchLevel()),
            () -> assertEquals(0, enchantments.fortuneLevel()),
            () -> assertEquals(4, enchantments.unbreakingLevel())
        );
    }

    @Test
    void hasEnchantmentTest2() {
        EnchantmentLevel.HasEnchantments enchantments = () -> List.of(new EnchantmentLevel(Enchantments.SILK_TOUCH, 1), new EnchantmentLevel(Enchantments.UNBREAKING, 4));
        assertAll(
            () -> assertEquals(0, enchantments.getLevel(Enchantments.BINDING_CURSE)),
            () -> assertEquals(0, enchantments.getLevel(Enchantments.BLOCK_EFFICIENCY)),
            () -> assertEquals(0, enchantments.fortuneLevel()),
            () -> assertEquals(1, enchantments.silktouchLevel()),
            () -> assertEquals(4, enchantments.unbreakingLevel())
        );
    }

    @Test
    void hasEnchantmentTest3() {
        EnchantmentLevel.HasEnchantments enchantments = () -> List.of(new EnchantmentLevel(Enchantments.BINDING_CURSE, 1), new EnchantmentLevel(Enchantments.FIRE_PROTECTION, 4));
        assertAll(
            () -> assertEquals(0, enchantments.fortuneLevel()),
            () -> assertEquals(0, enchantments.silktouchLevel()),
            () -> assertEquals(0, enchantments.unbreakingLevel()),
            () -> assertEquals(1, enchantments.getLevel(Enchantments.BINDING_CURSE)),
            () -> assertEquals(4, enchantments.getLevel(Enchantments.FIRE_PROTECTION))
        );
    }

    @Test
    void hasEnchantmentTest4() {
        EnchantmentLevel.HasEnchantments enchantments = () -> List.of(new EnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, 1), new EnchantmentLevel(Enchantments.FIRE_PROTECTION, 4));
        assertAll(
            () -> assertEquals(1, enchantments.efficiencyLevel()),
            () -> assertEquals(0, enchantments.fortuneLevel()),
            () -> assertEquals(0, enchantments.silktouchLevel()),
            () -> assertEquals(0, enchantments.unbreakingLevel()),
            () -> assertEquals(4, enchantments.getLevel(Enchantments.FIRE_PROTECTION))
        );
    }

    @Test
    void noEnchantments() {
        var enchantments = EnchantmentLevel.NoEnchantments.INSTANCE;
        assertAll(
            () -> assertEquals(0, enchantments.getLevel(Enchantments.BLOCK_EFFICIENCY)),
            () -> assertEquals(0, enchantments.efficiencyLevel()),
            () -> assertEquals(0, enchantments.fortuneLevel()),
            () -> assertEquals(0, enchantments.unbreakingLevel()),
            () -> assertEquals(0, enchantments.silktouchLevel()),
            () -> assertEquals(Collections.emptyList(), enchantments.getEnchantments())
        );
    }

    static Stream<Enchantment> enchantments() {
        return ForgeRegistries.ENCHANTMENTS.getValues().stream();
    }

    @Test
    void dummy() {
        assertTrue(enchantments().findAny().isPresent());
    }
}
