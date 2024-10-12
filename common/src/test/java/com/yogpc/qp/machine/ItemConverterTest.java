package com.yogpc.qp.machine;

import com.yogpc.qp.BeforeMC;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ItemConverterTest extends BeforeMC {
    @Test
    void createInstance() {
        var instance = assertDoesNotThrow(() -> new ItemConverter(List.of()));
        assertNotNull(instance);
    }

    static Stream<ItemStack> stacks() {
        return Stream.concat(Stream.of(
            Items.STONE,
            Items.DEEPSLATE,
            Items.DEEPSLATE_BRICKS,
            Items.APPLE,
            Items.BEDROCK,
            Items.POTION,
            Items.DIAMOND_ORE,
            Items.END_STONE
        ).map(Item::getDefaultInstance), Stream.of(ItemStack.EMPTY));
    }

    @ParameterizedTest
    @MethodSource("stacks")
    void noConversion(ItemStack stack) {
        var converter = new ItemConverter(List.of());
        var converted = converter.convert(stack).toList();
        assertEquals(1, converted.size());
        assertSame(stack, converted.getFirst());
    }

    @ParameterizedTest
    @MethodSource("stacks")
    void noConversionDeepslate(ItemStack stack) {
        var conversion = new ItemConverter.DeepslateOreConversion();
        var converter = new ItemConverter(List.of(conversion));
        assertFalse(conversion.shouldApply(stack));
        var converted = converter.convert(stack).toList();
        assertEquals(1, converted.size());
        assertSame(stack, converted.getFirst());
    }

    @ParameterizedTest
    @MethodSource
    void conversionDeepslate(ItemStack stack, ItemStack expected) {
        var converter = new ItemConverter.DeepslateOreConversion();
        assertTrue(converter.shouldApply(stack));
        var converted = converter.convert(stack).toList();
        assertEquals(1, converted.size());
        var convertedStack = converted.getFirst();
        assertEquals(expected.getCount(), convertedStack.getCount());
        assertEquals(MachineStorage.ItemKey.of(expected), MachineStorage.ItemKey.of(convertedStack));
    }

    static Stream<Arguments> conversionDeepslate() {
        return Stream.of(
            Arguments.of(Items.DEEPSLATE_COAL_ORE.getDefaultInstance(), Items.COAL_ORE.getDefaultInstance()),
            Arguments.of(Items.DEEPSLATE_IRON_ORE.getDefaultInstance(), Items.IRON_ORE.getDefaultInstance()),
            Arguments.of(Items.DEEPSLATE_GOLD_ORE.getDefaultInstance(), Items.GOLD_ORE.getDefaultInstance()),
            Arguments.of(Items.DEEPSLATE_REDSTONE_ORE.getDefaultInstance(), Items.REDSTONE_ORE.getDefaultInstance()),
            Arguments.of(Items.DEEPSLATE_LAPIS_ORE.getDefaultInstance(), Items.LAPIS_ORE.getDefaultInstance()),
            Arguments.of(Items.DEEPSLATE_EMERALD_ORE.getDefaultInstance(), Items.EMERALD_ORE.getDefaultInstance()),
            Arguments.of(Items.DEEPSLATE_DIAMOND_ORE.getDefaultInstance(), Items.DIAMOND_ORE.getDefaultInstance()),
            Arguments.of(Items.DEEPSLATE_COPPER_ORE.getDefaultInstance(), Items.COPPER_ORE.getDefaultInstance())
        );
    }

    @ParameterizedTest
    @MethodSource("stacks")
    void noConvertToEmptyConverter(ItemStack stack) {
        var conversion = new ItemConverter.ToEmptyConverter(Set.of(
            MachineStorage.ItemKey.of(Items.BREAD.getDefaultInstance()),
            MachineStorage.ItemKey.of(Items.WHEAT.getDefaultInstance())
        ));
        var converter = new ItemConverter(List.of(conversion));
        assertFalse(conversion.shouldApply(stack));
        var converted = converter.convert(stack).toList();
        assertEquals(1, converted.size());
        assertSame(stack, converted.getFirst());
    }

    @ParameterizedTest
    @MethodSource
    void convertToEmptyConverter(ItemStack stack) {
        var conversion = new ItemConverter.ToEmptyConverter(Set.of(
            MachineStorage.ItemKey.of(Items.BREAD.getDefaultInstance()),
            MachineStorage.ItemKey.of(Items.WHEAT.getDefaultInstance())
        ));
        assertTrue(conversion.shouldApply(stack));
        var converter = new ItemConverter(List.of(conversion));
        assertEquals(0, converter.convert(stack).count());
    }

    static Stream<ItemStack> convertToEmptyConverter() {
        return Stream.of(
            Items.BREAD.getDefaultInstance(),
            Items.WHEAT.getDefaultInstance()
        );
    }
}
