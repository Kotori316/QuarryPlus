package com.yogpc.qp.machines;

import java.util.stream.Stream;

import com.yogpc.qp.QuarryPlusTest;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(QuarryPlusTest.class)
class FluidKeyTest {
    @Test
    void equalSameFluid() {
        var key1 = new FluidKey(Fluids.WATER, null);
        var key2 = new FluidKey(Fluids.WATER, null);
        assertEquals(key1, key2);
    }

    @ParameterizedTest
    @MethodSource("noWater")
    void notEqualFluid(Fluid other) {
        var key1 = new FluidKey(Fluids.WATER, null);
        var key2 = new FluidKey(other, null);
        assertNotEquals(key1, key2);
    }

    static Stream<Fluid> noWater() {
        return Stream.of(Fluids.EMPTY, Fluids.FLOWING_WATER, Fluids.FLOWING_LAVA, Fluids.LAVA);
    }

    @ParameterizedTest
    @MethodSource("noWater")
    void notEqualTag(Fluid other) {
        var tag1 = new CompoundTag();
        tag1.putLong("test", 1);
        var key1 = new FluidKey(other, tag1);
        var key2 = new FluidKey(other, new CompoundTag());
        assertNotEquals(key1, key2);
    }

    @Test
    void equalTag() {
        var tag1 = new CompoundTag();
        tag1.putLong("test", 1);
        var key1 = new FluidKey(Fluids.WATER, tag1);
        var tag2 = tag1.copy();
        var key2 = new FluidKey(Fluids.WATER, tag2);
        assertEquals(key1, key2);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 16, 64, 1000, 5000, 16000})
    void ignoreAmount(int amount) {
        var key = new FluidKey(Fluids.WATER, null);
        var stack = new FluidStack(Fluids.WATER, amount);
        assertEquals(key, new FluidKey(stack));
    }

    @Test
    void toStack() {
        var tag1 = new CompoundTag();
        tag1.putLong("test", 1);
        var key1 = new FluidKey(Fluids.WATER, tag1);
        var tag2 = tag1.copy();
        var expect = new FluidStack(Fluids.WATER, 6500, tag2);
        var fromKey = key1.toStack(6500);
        assertTrue(expect.isFluidStackIdentical(fromKey), String.format("%s should be same as %s", fromKey, expect));
    }
}
