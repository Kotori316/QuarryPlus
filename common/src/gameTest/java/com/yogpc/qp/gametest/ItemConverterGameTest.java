package com.yogpc.qp.gametest;

import com.google.common.base.CaseFormat;
import com.yogpc.qp.machine.ItemConverter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public final class ItemConverterGameTest {
    public static Stream<TestFunction> converterTests(String batchName, String structureName) {
        return Stream.concat(
            noConversionChunkDestroyer(batchName, structureName),
            conversionChunkDestroyer(batchName, structureName)
        );
    }

    private static Stream<TestFunction> noConversionChunkDestroyer(String batchName, String structureName) {
        var keep = Stream.of(
            ItemStack.EMPTY,
            Items.APPLE.getDefaultInstance(),
            Items.BEDROCK.getDefaultInstance(),
            Items.POTION.getDefaultInstance(),
            Items.DIAMOND_ORE.getDefaultInstance(),
            Items.END_STONE.getDefaultInstance()
        );
        return keep.map(stack -> {
            var path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
            var name = "noConversionChunkDestroyer_%s".formatted(path);
            return new TestFunction(batchName, CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name), structureName, 5, 0, true, GameTestFunctions.wrapper(g -> {
                var converter = new ItemConverter.ChunkDestroyerConversion();
                assertFalse(converter.shouldApply(stack));
                var conversion = new ItemConverter(List.of(converter));
                var converted = conversion.convert(stack).toList();
                assertEquals(1, converted.size());
                assertSame(stack, converted.getFirst());
                g.succeed();
            }));
        });
    }

    private static Stream<TestFunction> conversionChunkDestroyer(String batchName, String structureName) {
        var stacks = Stream.of(
            Items.DIRT,
            Items.GRASS_BLOCK,
            Items.STONE,
            Items.COBBLESTONE,
            Items.DEEPSLATE,
            Items.GRANITE,
            Items.DIORITE,
            Items.ANDESITE,
            Items.TUFF,
            Items.NETHERRACK,
            Items.SANDSTONE,
            Items.RED_SANDSTONE
        ).map(Item::getDefaultInstance);
        return stacks.map(stack -> {
            var path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
            var name = "conversionChunkDestroyer_%s".formatted(path);
            return new TestFunction(batchName, CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name), structureName, 5, 0, true, GameTestFunctions.wrapper(g -> {
                var converter = new ItemConverter.ChunkDestroyerConversion();
                assertTrue(converter.shouldApply(stack));
                var conversion = new ItemConverter(List.of(converter));
                var count = conversion.convert(stack).count();
                assertEquals(0, count);
                g.succeed();
            }));
        });
    }
}
