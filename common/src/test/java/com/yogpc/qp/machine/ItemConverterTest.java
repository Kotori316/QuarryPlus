package com.yogpc.qp.machine;

import com.yogpc.qp.BeforeMC;
import net.minecraft.world.item.ItemStackTemplate;
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

    static Stream<ItemStackTemplate> stacks() {
        return Stream.of(
            Items.STONE,
            Items.DEEPSLATE,
            Items.DEEPSLATE_BRICKS,
            Items.APPLE,
            Items.BEDROCK,
            Items.POTION,
            Items.DIAMOND_ORE,
            Items.END_STONE
        ).map(ItemStackTemplate::new);
    }

    @ParameterizedTest
    @MethodSource("stacks")
    void noConversion(ItemStackTemplate stack) {
        var converter = new ItemConverter(List.of());
        var converted = converter.convert(stack).toList();
        assertEquals(1, converted.size());
        assertSame(stack, converted.getFirst());
    }

    @ParameterizedTest
    @MethodSource("stacks")
    void noConversionDeepslate(ItemStackTemplate stack) {
        var conversion = new ItemConverter.DeepslateOreConversion();
        var converter = new ItemConverter(List.of(conversion));
        assertFalse(conversion.shouldApply(stack));
        var converted = converter.convert(stack).toList();
        assertEquals(1, converted.size());
        assertSame(stack, converted.getFirst());
    }

    @ParameterizedTest
    @MethodSource
    void conversionDeepslate(ItemStackTemplate stack, ItemStackTemplate expected) {
        var converter = new ItemConverter.DeepslateOreConversion();
        assertTrue(converter.shouldApply(stack));
        var converted = converter.convert(stack).toList();
        assertEquals(1, converted.size());
        var convertedStack = converted.getFirst();
        assertEquals(expected.count(), convertedStack.count());
        assertEquals(MachineStorage.ItemKey.of(expected), MachineStorage.ItemKey.of(convertedStack));
    }

    static Stream<Arguments> conversionDeepslate() {
        return Stream.of(
            Arguments.of(new ItemStackTemplate(Items.DEEPSLATE_COAL_ORE), new ItemStackTemplate(Items.COAL_ORE)),
            Arguments.of(new ItemStackTemplate(Items.DEEPSLATE_IRON_ORE), new ItemStackTemplate(Items.IRON_ORE)),
            Arguments.of(new ItemStackTemplate(Items.DEEPSLATE_GOLD_ORE), new ItemStackTemplate(Items.GOLD_ORE)),
            Arguments.of(new ItemStackTemplate(Items.DEEPSLATE_REDSTONE_ORE), new ItemStackTemplate(Items.REDSTONE_ORE)),
            Arguments.of(new ItemStackTemplate(Items.DEEPSLATE_LAPIS_ORE), new ItemStackTemplate(Items.LAPIS_ORE)),
            Arguments.of(new ItemStackTemplate(Items.DEEPSLATE_EMERALD_ORE), new ItemStackTemplate(Items.EMERALD_ORE)),
            Arguments.of(new ItemStackTemplate(Items.DEEPSLATE_DIAMOND_ORE), new ItemStackTemplate(Items.DIAMOND_ORE)),
            Arguments.of(new ItemStackTemplate(Items.DEEPSLATE_COPPER_ORE), new ItemStackTemplate(Items.COPPER_ORE))
        );
    }

    @ParameterizedTest
    @MethodSource("stacks")
    void noConvertToEmptyConverter(ItemStackTemplate stack) {
        var conversion = new ItemConverter.ToEmptyConverter(Set.of(
            MachineStorage.ItemKey.of(new ItemStackTemplate(Items.BREAD)),
            MachineStorage.ItemKey.of(new ItemStackTemplate(Items.WHEAT))
        ));
        var converter = new ItemConverter(List.of(conversion));
        assertFalse(conversion.shouldApply(stack));
        var converted = converter.convert(stack).toList();
        assertEquals(1, converted.size());
        assertSame(stack, converted.getFirst());
    }

    @ParameterizedTest
    @MethodSource
    void convertToEmptyConverter(ItemStackTemplate stack) {
        var conversion = new ItemConverter.ToEmptyConverter(Set.of(
            MachineStorage.ItemKey.of(new ItemStackTemplate(Items.BREAD)),
            MachineStorage.ItemKey.of(new ItemStackTemplate(Items.WHEAT))
        ));
        assertTrue(conversion.shouldApply(stack));
        var converter = new ItemConverter(List.of(conversion));
        assertEquals(0, converter.convert(stack).count());
    }

    static Stream<ItemStackTemplate> convertToEmptyConverter() {
        return Stream.of(
            Items.BREAD,
            Items.WHEAT
        ).map(ItemStackTemplate::new);
    }

    @ParameterizedTest
    @MethodSource("concatTarget")
    void concat(ItemStackTemplate stack) {
        var converter = new ItemConverter(List.of(
            new ItemConverter.ToEmptyConverter(Set.of(
                MachineStorage.ItemKey.of(new ItemStackTemplate(Items.BREAD))
            )),
            new ItemConverter.DeepslateOreConversion())
        );

        var converted = converter.convert(stack).toList();
        if (!converted.isEmpty()) {
            assertNotSame(stack, converted.getFirst());
        }
    }

    static Stream<ItemStackTemplate> concatTarget() {
        return Stream.concat(
            Stream.of(
                new ItemStackTemplate(Items.BREAD)
            ),
            conversionDeepslate().map(a -> (ItemStackTemplate) a.get()[0])
        );
    }
}
