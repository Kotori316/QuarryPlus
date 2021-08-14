package com.yogpc.qp.machines;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import com.yogpc.qp.QuarryPlusTest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemConverterTest extends QuarryPlusTest {
    private static ItemKey toKey(Item item) {
        return new ItemKey(item, null);
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

    @ParameterizedTest
    @MethodSource("pickaxeConvert")
    void dynamicConversion1(Item material, Item pickaxe) {
        var keys = Set.of(toKey(Items.IRON_INGOT), toKey(Items.GOLD_INGOT), toKey(Items.DIAMOND), toKey(Items.STONE));
        Function<ItemKey, ItemKey> convertFunction = i -> {
            var name = i.getId();
            var pickaxeName = name.getPath().replace("_ingot", "").replace("gold", "golden") + "_pickaxe";
            var pickaxeItem = Registry.ITEM.get(new Identifier(name.getNamespace(), pickaxeName));
            return new ItemKey(pickaxeItem, i.nbt());
        };
        var converter = new ItemConverter(List.of(Pair.of(keys::contains, convertFunction)));

        var converted = converter.map(new ItemStack(material));
        assertEquals(pickaxe, converted.getItem());
    }

    @ParameterizedTest
    @MethodSource("deepOres")
    void dynamicDeepSlate(Item expected, Item original) {
        var converter = ItemConverter.deepslateConverter();
        var converted = converter.map(new ItemStack(original));
        assertEquals(expected, converted.getItem());
    }

    static Stream<Object[]> pickaxeConvert() {
        return Stream.of(
            Pair.of(Items.LAPIS_LAZULI, Items.LAPIS_LAZULI),
            Pair.of(Items.GOLDEN_APPLE, Items.GOLDEN_APPLE),
            Pair.of(Items.STONE, Items.STONE_PICKAXE),
            Pair.of(Items.IRON_INGOT, Items.IRON_PICKAXE),
            Pair.of(Items.GOLD_INGOT, Items.GOLDEN_PICKAXE),
            Pair.of(Items.DIAMOND, Items.DIAMOND_PICKAXE)
        ).map(p -> new Object[]{p.getKey(), p.getValue()});
    }

    static Stream<Object[]> deepOres() {
        return Stream.of(
            Pair.of(Items.LAPIS_LAZULI, Items.LAPIS_LAZULI),
            Pair.of(Items.GOLDEN_APPLE, Items.GOLDEN_APPLE),
            Pair.of(Items.COAL_ORE, Items.DEEPSLATE_COAL_ORE),
            Pair.of(Items.IRON_ORE, Items.DEEPSLATE_IRON_ORE),
            Pair.of(Items.GOLD_ORE, Items.DEEPSLATE_GOLD_ORE),
            Pair.of(Items.DIAMOND_ORE, Items.DEEPSLATE_DIAMOND_ORE),
            Pair.of(Items.REDSTONE_ORE, Items.DEEPSLATE_REDSTONE_ORE),
            Pair.of(Items.LAPIS_ORE, Items.DEEPSLATE_LAPIS_ORE)
        ).map(p -> new Object[]{p.getKey(), p.getValue()});
    }

    @Test
    void dummy() {
        assertTrue(pickaxeConvert().count() > 0);
        assertTrue(deepOres().count() > 0);
    }
}
