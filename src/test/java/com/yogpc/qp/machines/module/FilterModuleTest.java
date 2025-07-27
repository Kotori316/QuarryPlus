package com.yogpc.qp.machines.module;

import com.yogpc.qp.machines.ItemKey;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    void instance20() {
        var itemKeys = List.of(
            new ItemKey(Items.STONE, null),
            new ItemKey(Items.GRANITE, null),
            new ItemKey(Items.POLISHED_GRANITE, null),
            new ItemKey(Items.DIORITE, null),
            new ItemKey(Items.POLISHED_DIORITE, null),
            new ItemKey(Items.ANDESITE, null),
            new ItemKey(Items.POLISHED_ANDESITE, null),
            new ItemKey(Items.DEEPSLATE, null),
            new ItemKey(Items.COBBLED_DEEPSLATE, null),
            new ItemKey(Items.POLISHED_DEEPSLATE, null),
            new ItemKey(Items.CALCITE, null),
            new ItemKey(Items.TUFF, null),
            new ItemKey(Items.DRIPSTONE_BLOCK, null),
            new ItemKey(Items.GRASS_BLOCK, null),
            new ItemKey(Items.DIRT, null),
            new ItemKey(Items.COARSE_DIRT, null),
            new ItemKey(Items.PODZOL, null),
            new ItemKey(Items.ROOTED_DIRT, null),
            new ItemKey(Items.MUD, null),
            new ItemKey(Items.CRIMSON_NYLIUM, null)
        );
        assertEquals(20, itemKeys.size());
        var module = new FilterModule(itemKeys);
        assertEquals(itemKeys, module.getItemKeys());
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