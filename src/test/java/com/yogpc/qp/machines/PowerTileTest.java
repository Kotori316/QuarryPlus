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

class PowerTileTest extends QuarryPlusTest {
    final static class TestConfigAccessor implements EnergyConfigAccessor {
        @Override
        public double makeFrame() {
            return 15d;
        }

        @Override
        public double moveHead() {
            return 0.5d;
        }

        @Override
        public double breakBlock() {
            return 10d;
        }

        @Override
        public double removeFluid() {
            return breakBlock() * 5;
        }
    }

    record Holder(Map<Enchantment, Integer> enchantmentMap) implements EnchantmentLevel.HasEnchantments {
        @Override
        public List<EnchantmentLevel> getEnchantments() {
            return enchantmentMap.entrySet().stream().map(EnchantmentLevel::new).toList();
        }

        @Override
        public int getLevel(Enchantment enchantment) {
            return enchantmentMap().getOrDefault(enchantment, 0);
        }

        @Override
        public EnergyConfigAccessor getAccessor() {
            return new TestConfigAccessor();
        }
    }

    @Test
    void makeFrame() {
        var makeFrameBase = PowerTile.Constants.getMakeFrameEnergy(new Holder(Map.of()));
        assertTrue(makeFrameBase > PowerTile.ONE_FE);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    void makeFrameUnbreakingTest(int unbreaking) {
        var makeFrameBase = PowerTile.Constants.getMakeFrameEnergy(new Holder(Map.of()));
        var reduced = PowerTile.Constants.getMakeFrameEnergy(new Holder(Map.of(Enchantments.UNBREAKING, unbreaking)));
        assertEquals(makeFrameBase / (1 + unbreaking), reduced);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -100, 0})
    void makeFrameInvalidLevel(int unbreaking) {
        var makeFrameBase = PowerTile.Constants.getMakeFrameEnergy(new Holder(Map.of()));
        var notChanged = PowerTile.Constants.getMakeFrameEnergy(new Holder(Map.of(Enchantments.UNBREAKING, unbreaking)));
        assertEquals(makeFrameBase, notChanged);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    void breakFluidUnbreakingTest(int unbreaking) {
        var breakFluidBase = PowerTile.Constants.getBreakBlockFluidEnergy(new Holder(Map.of()));
        var reduced = PowerTile.Constants.getBreakBlockFluidEnergy(new Holder(Map.of(Enchantments.UNBREAKING, unbreaking)));
        assertEquals(breakFluidBase / (1 + unbreaking), reduced);
    }

    @Test
    void breakEnergy0() {
        var base = PowerTile.Constants.getBreakEnergy(0, new Holder(Map.of()));
        assertEquals(0, base);
    }

    @Test
    void breakEnergyNaN() {
        var base = PowerTile.Constants.getBreakEnergy(Float.NaN, new Holder(Map.of()));
        assertEquals(0, base);
    }

    @Test
    void breakEnergy1() {
        var base = PowerTile.Constants.getBreakEnergy(1, new Holder(Map.of()));
        assertTrue(base > 0);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void breakBlockUnbreakingTest(int unbreaking) {
        var breakFluidBase = PowerTile.Constants.getBreakEnergy(1, new Holder(Map.of()));
        var reduced = PowerTile.Constants.getBreakEnergy(1, new Holder(Map.of(Enchantments.UNBREAKING, unbreaking)));
        assertEquals(breakFluidBase / (1 + unbreaking), reduced);
    }

    @ParameterizedTest
    @ValueSource(floats = {-1, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY})
    void breakUnbreakable(float unbreakableHardness) {
        var base = PowerTile.Constants.getBreakEnergy(unbreakableHardness, new Holder(Map.of()));
        assertTrue(base > 0);
        assertAll(
            IntStream.range(0, 10).mapToObj(unbreaking -> new Holder(Map.of(Enchantments.UNBREAKING, unbreaking)))
                .map(h -> PowerTile.Constants.getBreakEnergy(unbreakableHardness, h))
                .map(required -> () -> assertEquals(base, required))
        );
    }
}
