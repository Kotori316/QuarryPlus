package com.yogpc.qp.machines;

import java.util.Map;

import com.yogpc.qp.QuarryPlusTest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemConverterTest extends QuarryPlusTest {
    private static MachineStorage.ItemKey toKey(Item item) {
        return new MachineStorage.ItemKey(item, null);
    }

    @Test
    void instance() {
        var converter = new ItemConverter(Map.of(
            toKey(Items.COBBLESTONE), toKey(Items.STONE)
        ));
        assertNotNull(converter);
    }

    @Test
    void conversion1() {
        var converter = new ItemConverter(Map.of(
            toKey(Items.COBBLESTONE), toKey(Items.STONE)
        ));
        var converted = converter.map(new ItemStack(Items.COBBLESTONE));
        assertEquals(Items.STONE, converted.getItem());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 5, 16, 64})
    void conversion2(int count) {
        var converter = new ItemConverter(Map.of(
            toKey(Items.COBBLESTONE), toKey(Items.APPLE)
        ));
        var converted = converter.map(new ItemStack(Items.COBBLESTONE, count));
        assertEquals(Items.APPLE, converted.getItem());
        assertEquals(count, converted.getCount());
    }

    @Test
    void conversion3() {
        var converter = new ItemConverter(Map.of(
            toKey(Items.COBBLESTONE), toKey(Items.STONE),
            toKey(Items.APPLE), toKey(Items.ENCHANTED_GOLDEN_APPLE)
        ));
        {
            var converted = converter.map(new ItemStack(Items.COBBLESTONE));
            assertEquals(Items.STONE, converted.getItem());
        }
        {
            var converted = converter.map(new ItemStack(Items.APPLE));
            assertEquals(Items.ENCHANTED_GOLDEN_APPLE, converted.getItem());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 5, 16, 64})
    void noConversion(int count) {
        var converter = new ItemConverter(Map.of(
            toKey(Items.COBBLESTONE), toKey(Items.APPLE)
        ));
        var before = new ItemStack(Items.STONE, count);
        var converted = converter.map(before);
        assertTrue(ItemStack.areEqual(before, converted), "Comparing of %s, %s".formatted(before, converted));
    }
}
