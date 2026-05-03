package com.yogpc.qp.gametest;

import com.google.common.base.CaseFormat;
import com.kotori316.testutil.common.TestFunction;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.ItemConverter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public final class ItemConverterGameTest {
    public static Stream<TestFunction> converterTests(String batchName, String structureName) {
        return Stream.of(
            emptyNoConversion(batchName, structureName),
            noConversionChunkDestroyer(batchName, structureName),
            conversionChunkDestroyer(batchName, structureName)
        ).flatMap(Function.identity());
    }

    private static Stream<TestFunction> emptyNoConversion(String batchName, String structureName) {
        var name = "emptyNoConversion";
        return Stream.of(
            TestFunction.createWithStructure(QuarryPlus.modID, batchName, CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name), structureName, g -> {
                var stack = ItemStack.EMPTY;
                var converter = new ItemConverter.ChunkDestroyerConversion();
                assertFalse(converter.shouldApply(stack));
                var conversion = new ItemConverter(List.of(converter));
                var converted = conversion.convert(stack).toList();
                assertEquals(0, converted.size());
                g.succeed();
            })
        );
    }

    private static Stream<TestFunction> noConversionChunkDestroyer(String batchName, String structureName) {
        var keep = Stream.of(
            Identifier.fromNamespaceAndPath("minecraft", "apple"),
            Identifier.fromNamespaceAndPath("minecraft", "bedrock"),
            Identifier.fromNamespaceAndPath("minecraft", "potion"),
            Identifier.fromNamespaceAndPath("minecraft", "diamond_ore"),
            Identifier.fromNamespaceAndPath("minecraft", "end_stone")
        );
        return keep.map(location -> {
            var path = location.getPath();
            var name = "noConversionChunkDestroyer_%s".formatted(path);
            return TestFunction.createWithStructure(QuarryPlus.modID, batchName, CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name), structureName, g -> {
                var stack = new ItemStack(BuiltInRegistries.ITEM.getValue(location));
                var converter = new ItemConverter.ChunkDestroyerConversion();
                assertFalse(converter.shouldApply(stack));
                var conversion = new ItemConverter(List.of(converter));
                var converted = conversion.convert(stack).toList();
                assertEquals(1, converted.size());
                assertEquals(ItemStackTemplate.fromNonEmptyStack(stack), ItemStackTemplate.fromNonEmptyStack(converted.getFirst()));
                g.succeed();
            });
        });
    }

    private static Stream<TestFunction> conversionChunkDestroyer(String batchName, String structureName) {
        var items = Stream.of(
            Identifier.fromNamespaceAndPath("minecraft", "dirt"),
            Identifier.fromNamespaceAndPath("minecraft", "coarse_dirt"),
            Identifier.fromNamespaceAndPath("minecraft", "rooted_dirt"),
            Identifier.fromNamespaceAndPath("minecraft", "grass_block"),
            Identifier.fromNamespaceAndPath("minecraft", "podzol"),
            Identifier.fromNamespaceAndPath("minecraft", "stone"),
            Identifier.fromNamespaceAndPath("minecraft", "cobblestone"),
            Identifier.fromNamespaceAndPath("minecraft", "deepslate"),
            Identifier.fromNamespaceAndPath("minecraft", "granite"),
            Identifier.fromNamespaceAndPath("minecraft", "diorite"),
            Identifier.fromNamespaceAndPath("minecraft", "andesite"),
            Identifier.fromNamespaceAndPath("minecraft", "tuff"),
            Identifier.fromNamespaceAndPath("minecraft", "netherrack"),
            Identifier.fromNamespaceAndPath("minecraft", "sandstone"),
            Identifier.fromNamespaceAndPath("minecraft", "red_sandstone")
        );
        return items.map(location -> {
            var path = location.getPath();
            var name = "conversionChunkDestroyer_%s".formatted(path);
            return TestFunction.createWithStructure(QuarryPlus.modID, batchName, CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name), structureName, g -> {
                var stack = new ItemStack(BuiltInRegistries.ITEM.getValue(location));
                var converter = new ItemConverter.ChunkDestroyerConversion();
                assertTrue(converter.shouldApply(stack));
                var conversion = new ItemConverter(List.of(converter));
                var count = conversion.convert(stack).count();
                assertEquals(0, count);
                g.succeed();
            });
        });
    }
}
