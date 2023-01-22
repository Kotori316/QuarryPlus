package com.yogpc.qp.machines.module;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.yogpc.qp.machines.ItemKey;
import net.minecraft.nbt.ListTag;
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
        assertTrue(module.getItemKeys().isEmpty());
    }

    @Test
    void emptyInstance2() {
        var module = new FilterModule((ListTag) null);
        assertTrue(module.getItemKeys().isEmpty());
    }

    @Test
    void instance() {
        var module = new FilterModule(List.of(new ItemKey(Items.BEDROCK, null)));
        assertEquals(List.of(new ItemKey(Items.BEDROCK, null)), module.getItemKeys());
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

    @Nested
    class SerializeTest {
        @Test
        void getFromNullTag() {
            assertTrue(FilterModule.getFromTag(null).isEmpty());
        }

        @Test
        void getFromEmptyTag() {
            assertTrue(FilterModule.getFromTag(new ListTag()).isEmpty());
        }

        @Test
        void getFromEmptyItems() {
            assertTrue(FilterModule.getFromItems(List.of()).isEmpty());
        }

        @Test
        void cycle() {
            var keys = items1().map(i -> new ItemKey(i, null)).toList();
            var listTag = FilterModule.getFromItemKeys(keys.stream());
            var fromTag = FilterModule.getFromTag(listTag);
            assertEquals(Set.copyOf(keys), Set.copyOf(fromTag));
        }
    }

    static Stream<Item> items1() {
        return Stream.of(
            Items.STONE,
            Items.ANDESITE,
            Items.POLISHED_GRANITE);
    }
}