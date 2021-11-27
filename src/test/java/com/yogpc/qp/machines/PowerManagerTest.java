package com.yogpc.qp.machines;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import com.yogpc.qp.QuarryPlusTest;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PowerManagerTest extends QuarryPlusTest {

    record Holder(Map<Enchantment, Integer> enchantmentMap) implements EnchantmentLevel.HasEnchantments {
        @Override
        public List<EnchantmentLevel> getEnchantments() {
            return enchantmentMap.entrySet().stream().map(EnchantmentLevel::new).toList();
        }

        @Override
        public int getLevel(Enchantment enchantment) {
            return enchantmentMap().getOrDefault(enchantment, 0);
        }
    }

    @Test
    void makeFrame() {
        var makeFrameBase = PowerManager.getMakeFrameEnergy(new Holder(Map.of()));
        assertTrue(makeFrameBase > PowerTile.ONE_FE);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    void makeFrameUnbreakingTest(int unbreaking) {
        var makeFrameBase = PowerManager.getMakeFrameEnergy(new Holder(Map.of()));
        var reduced = PowerManager.getMakeFrameEnergy(new Holder(Map.of(Enchantments.UNBREAKING, unbreaking)));
        assertEquals(makeFrameBase / (1 + unbreaking), reduced);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -100, 0})
    void makeFrameInvalidLevel(int unbreaking) {
        var makeFrameBase = PowerManager.getMakeFrameEnergy(new Holder(Map.of()));
        var notChanged = PowerManager.getMakeFrameEnergy(new Holder(Map.of(Enchantments.UNBREAKING, unbreaking)));
        assertEquals(makeFrameBase, notChanged);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    void breakFluidUnbreakingTest(int unbreaking) {
        var breakFluidBase = PowerManager.getBreakBlockFluidEnergy(new Holder(Map.of()));
        var reduced = PowerManager.getBreakBlockFluidEnergy(new Holder(Map.of(Enchantments.UNBREAKING, unbreaking)));
        assertEquals(breakFluidBase / (1 + unbreaking), reduced);
    }

    @Test
    void breakEnergy0() {
        var base = PowerManager.getBreakEnergy(0, new Holder(Map.of()));
        assertEquals(0, base);
    }

    @Test
    void breakEnergyNaN() {
        var base = PowerManager.getBreakEnergy(Float.NaN, new Holder(Map.of()));
        assertEquals(0, base);
    }

    @Test
    void breakEnergyInfinity() {
        var base = PowerManager.getBreakEnergy(Float.POSITIVE_INFINITY, new Holder(Map.of()));
        var hardness200 = PowerManager.getBreakEnergy(200f, new Holder(Map.of()));
        assertEquals(hardness200, base);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    void breakEnergyInfinityIgnoresUnbreaking(int unbreaking) {
        var base = PowerManager.getBreakEnergy(Float.POSITIVE_INFINITY, new Holder(Map.of()));
        var withUnbreaking = PowerManager.getBreakEnergy(Float.POSITIVE_INFINITY, new Holder(Map.of(Enchantments.UNBREAKING, unbreaking)));
        assertEquals(base, withUnbreaking);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6})
    void breakEnergyInfinityEfficiency(int efficiency) {
        var base = PowerManager.getBreakEnergy(Float.POSITIVE_INFINITY, new Holder(Map.of(Enchantments.BLOCK_EFFICIENCY, efficiency)));
        var hardness200 = PowerManager.getBreakEnergy(200f, new Holder(Map.of(Enchantments.BLOCK_EFFICIENCY, efficiency)));
        assertTrue(base >= hardness200, "Infinity: %d, 200: %d".formatted(base, hardness200));
    }

    @Test
    void breakEnergy1() {
        var base = PowerManager.getBreakEnergy(1, new Holder(Map.of()));
        assertTrue(base > 0);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void breakBlockUnbreakingTest(int unbreaking) {
        var breakFluidBase = PowerManager.getBreakEnergy(1, new Holder(Map.of()));
        var reduced = PowerManager.getBreakEnergy(1, new Holder(Map.of(Enchantments.UNBREAKING, unbreaking)));
        assertEquals(breakFluidBase / (1 + unbreaking), reduced);
    }

    @ParameterizedTest
    @ValueSource(floats = {-1, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY})
    void breakUnbreakable(float unbreakableHardness) {
        var base = PowerManager.getBreakEnergy(unbreakableHardness, new Holder(Map.of()));
        assertTrue(base > 0);
        assertAll(
            IntStream.range(0, 10).mapToObj(unbreaking -> new Holder(Map.of(Enchantments.UNBREAKING, unbreaking)))
                .map(h -> PowerManager.getBreakEnergy(unbreakableHardness, h))
                .map(required -> () -> assertEquals(base, required))
        );
    }
}
