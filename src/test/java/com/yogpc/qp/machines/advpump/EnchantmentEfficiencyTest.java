package com.yogpc.qp.machines.advpump;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.yogpc.qp.QuarryPlusTest;
import com.yogpc.qp.machines.EnchantmentLevel;
import com.yogpc.qp.machines.PowerTile;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.registries.ForgeRegistries;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnchantmentEfficiencyTest extends QuarryPlusTest {
    @Test
    void dummy() {
        assertTrue(randomEnchantments().findAny().isPresent());
    }

    @Test
    void emptyInstance() {
        EnchantmentEfficiency enchantmentEfficiency = new EnchantmentEfficiency(List.of());
        assertNotNull(enchantmentEfficiency);
        assertEquals(List.of(), enchantmentEfficiency.getEnchantments());
        assertEquals(100 * PowerTile.ONE_FE, enchantmentEfficiency.baseEnergy);
    }

    @Test
    void emptyNbt() {
        var deserialized = EnchantmentEfficiency.fromNbt(new CompoundTag());
        assertEquals(List.of(), deserialized.getEnchantments());
    }

    @ParameterizedTest(name = "[" + ParameterizedTest.INDEX_PLACEHOLDER + "]")
    @MethodSource("randomEnchantments")
    @EmptySource
    void serialize(List<EnchantmentLevel> list) {
        var enchantmentEfficiency = new EnchantmentEfficiency(list);
        assertEquals(list, enchantmentEfficiency.getEnchantments());
        var tag = enchantmentEfficiency.toNbt();
        assertEquals(list.isEmpty(), tag.isEmpty());
        var deserialized = EnchantmentEfficiency.fromNbt(tag);
        assertEquals(list, deserialized.getEnchantments());
    }

    @Test
    void unbreaking() {
        var b0 = new EnchantmentEfficiency(List.of());
        var b1 = new EnchantmentEfficiency(List.of(new EnchantmentLevel(Enchantments.UNBREAKING, 1)));
        var b2 = new EnchantmentEfficiency(List.of(new EnchantmentLevel(Enchantments.UNBREAKING, 2)));
        var b3 = new EnchantmentEfficiency(List.of(new EnchantmentLevel(Enchantments.UNBREAKING, 3)));
        var b4 = new EnchantmentEfficiency(List.of(new EnchantmentLevel(Enchantments.UNBREAKING, 4)));
        assertAll(
            () -> assertTrue(b0.baseEnergy > b1.baseEnergy),
            () -> assertTrue(b1.baseEnergy > b2.baseEnergy),
            () -> assertTrue(b2.baseEnergy > b3.baseEnergy),
            () -> assertEquals(b3.baseEnergy, b4.baseEnergy)
        );
    }

    @Test
    void range() {
        var f0 = new EnchantmentEfficiency(List.of());
        var f1 = new EnchantmentEfficiency(List.of(new EnchantmentLevel(Enchantments.BLOCK_FORTUNE, 1)));
        var f2 = new EnchantmentEfficiency(List.of(new EnchantmentLevel(Enchantments.BLOCK_FORTUNE, 2)));
        var f3 = new EnchantmentEfficiency(List.of(new EnchantmentLevel(Enchantments.BLOCK_FORTUNE, 3)));
        var s1 = new EnchantmentEfficiency(List.of(new EnchantmentLevel(Enchantments.SILK_TOUCH, 1)));
        var s2 = new EnchantmentEfficiency(List.of(new EnchantmentLevel(Enchantments.SILK_TOUCH, 2)));

        assertAll(
            () -> assertTrue(f0.range < f1.range),
            () -> assertTrue(f1.range < f2.range),
            () -> assertTrue(f2.range < f3.range),
            () -> assertEquals(f3.range, s1.range),
            () -> assertEquals(s2.range, s1.range)
        );
    }

    @Test
    void energyCapacity() {
        var e0 = new EnchantmentEfficiency(List.of());
        var e1 = new EnchantmentEfficiency(List.of(new EnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, 1)));
        var e2 = new EnchantmentEfficiency(List.of(new EnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, 2)));
        var e3 = new EnchantmentEfficiency(List.of(new EnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, 3)));
        var e4 = new EnchantmentEfficiency(List.of(new EnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, 4)));
        var e5 = new EnchantmentEfficiency(List.of(new EnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, 5)));
        assertAll(
            () -> assertTrue(e0.energyCapacity < e1.energyCapacity),
            () -> assertTrue(e1.energyCapacity < e2.energyCapacity),
            () -> assertTrue(e2.energyCapacity < e3.energyCapacity),
            () -> assertTrue(e3.energyCapacity < e4.energyCapacity),
            () -> assertTrue(e4.energyCapacity < e5.energyCapacity)
        );
    }

    @Test
    void energyCapacityRange() {
        var e0 = new EnchantmentEfficiency(List.of());
        assertTrue(Math.log10((double) e0.energyCapacity / PowerTile.ONE_FE) < 4);
    }

    @Test
    void fluidCapacity() {
        var e0 = new EnchantmentEfficiency(List.of());
        var e1 = new EnchantmentEfficiency(List.of(new EnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, 1)));
        var e2 = new EnchantmentEfficiency(List.of(new EnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, 2)));
        var e3 = new EnchantmentEfficiency(List.of(new EnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, 3)));
        var e4 = new EnchantmentEfficiency(List.of(new EnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, 4)));
        var e5 = new EnchantmentEfficiency(List.of(new EnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, 5)));
        assertAll(
            () -> assertTrue(e0.fluidCapacity < e1.fluidCapacity),
            () -> assertTrue(e1.fluidCapacity < e2.fluidCapacity),
            () -> assertTrue(e2.fluidCapacity < e3.fluidCapacity),
            () -> assertTrue(e3.fluidCapacity < e4.fluidCapacity),
            () -> assertTrue(e4.fluidCapacity < e5.fluidCapacity)
        );
    }

    static Stream<List<EnchantmentLevel>> randomEnchantments() {
        Random random = new Random(864);
        List<Enchantment> enchantments = List.copyOf(ForgeRegistries.ENCHANTMENTS.getValues());

        return IntStream.range(0, 50).mapToObj(i ->
            IntStream.range(0, random.nextInt(10) + 1)
                .mapToObj(ii -> Util.getRandom(enchantments, random))
                .distinct()
                .map(e -> new EnchantmentLevel(e, random.nextInt(e.getMaxLevel()) + 1))
                .sorted(EnchantmentLevel.QUARRY_ENCHANTMENT_COMPARATOR)
                .toList());
    }
}
