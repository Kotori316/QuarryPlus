package com.yogpc.qp.machines.module;

import java.util.List;
import java.util.stream.Stream;

import com.yogpc.qp.machines.ItemKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilterModuleTest {
    @Test
    void emptyInstance() {
        var module = new FilterModule(List.of());
        assertTrue(module.itemKeys.isEmpty());
    }

    @Test
    void instance() {
        var module = new FilterModule(List.of(new ItemKey(Items.BEDROCK, null)));
        assertEquals(List.of(new ItemKey(Items.BEDROCK, null)), module.itemKeys);
    }

    @Nested
    class ConverterTest {
        @Test
        void empty() {
            var module = new FilterModule(List.of());
            var converter = module.createConverter();
            assertAll(items1().map(ItemStack::new).map(converter::map).map(i ->
                () -> assertFalse(i.isEmpty())
            ));
        }

        @Test
        void cobblestone() {
            var module = new FilterModule(List.of(new ItemKey(Items.COBBLESTONE, null)));
            var converter = module.createConverter();
            assertAll(items1().map(ItemStack::new).map(converter::map).map(i ->
                () -> assertFalse(i.isEmpty())
            ));
            assertTrue(converter.map(new ItemStack(Items.COBBLESTONE)).isEmpty());
        }

        @Test
        void cobblestoneAndBedrock() {
            var module = new FilterModule(List.of(new ItemKey(Items.COBBLESTONE, null), new ItemKey(Items.BEDROCK, null)));
            var converter = module.createConverter();
            assertAll(items1().map(ItemStack::new).map(converter::map).map(i ->
                () -> assertFalse(i.isEmpty())
            ));
            assertTrue(converter.map(new ItemStack(Items.COBBLESTONE)).isEmpty());
            assertTrue(converter.map(new ItemStack(Items.BEDROCK)).isEmpty());
        }
    }

    static Stream<Item> items1() {
        return Stream.of(
            Items.STONE,
            Items.ANDESITE,
            Items.POLISHED_GRANITE);
    }
}