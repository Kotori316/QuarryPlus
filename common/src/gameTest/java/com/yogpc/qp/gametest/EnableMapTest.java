package com.yogpc.qp.gametest;

import com.google.common.base.CaseFormat;
import com.kotori316.testutil.common.TestFunction;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.config.EnableMap;
import com.yogpc.qp.machine.QpItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public final class EnableMapTest {

    public static Stream<TestFunction> test(String batchName, String structureName) {
        return Stream.concat(
            configured(batchName, structureName),
            hasValidBlocks(batchName, structureName)
        );
    }

    private static Stream<TestFunction> configured(String batchName, String structureName) {
        var name = "EnableMapTest";
        return Stream.of(
            TestFunction.createWithStructure(QuarryPlus.modID, batchName, CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name), structureName, g -> {
                var blockEntityTypes = BuiltInRegistries.BLOCK_ENTITY_TYPE.keySet()
                    .stream()
                    .filter(e -> e.getNamespace().equals(QuarryPlus.modID));
                var items = BuiltInRegistries.ITEM.entrySet().stream()
                    .filter(e -> e.getValue() instanceof QpItem)
                    .map(Map.Entry::getKey)
                    .map(ResourceKey::identifier);
                Stream.concat(blockEntityTypes, items).forEach(e -> {
                    if (!PlatformAccess.getAccess().registerObjects().defaultEnableSetting().containsKey(e.getPath())) {
                        g.fail(Component.literal("%s is not configured".formatted(e.getPath())));
                    }
                });
                g.succeed();
            })
        );
    }

    private static Stream<TestFunction> hasValidBlocks(String batchName, String structureName) {
        var name = "EnableMapTestHasValidBlock";
        return Stream.of(
            TestFunction.createWithStructure(QuarryPlus.modID, batchName, CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name), structureName, g -> {
                var blockEntityTypes = BuiltInRegistries.BLOCK_ENTITY_TYPE.entrySet()
                    .stream()
                    .filter(e -> e.getKey().identifier().getNamespace().equals(QuarryPlus.modID))
                    .toList();
                try {
                    var field = BlockEntityType.class.getDeclaredField("validBlocks");
                    field.setAccessible(true);
                    for (var e : blockEntityTypes) {
                        var b = e.getValue();
                        var blocks = (Set<?>) field.get(b);
                        assertFalse(blocks.isEmpty());
                    }
                    g.succeed();
                } catch (ReflectiveOperationException exception) {
                    throw new RuntimeException(exception);
                }
            })
        );
    }

    public static void createInstance(GameTestHelper helper) {
        assertDoesNotThrow(() -> new EnableMap());
        helper.succeed();
    }

    public static void getFromEmptyMap(GameTestHelper helper) {
        var map = new EnableMap();
        assertAll(Stream.of("quarry", "pump").map(name -> () -> assertFalse(map.enabled(name))));
        helper.succeed();
    }

    public static void getFromMap(GameTestHelper helper) {
        var map = new EnableMap(Map.of(
            "quarry", true
        ));
        assertTrue(map.enabled("quarry"));
        assertFalse(map.enabled("pump"));
        helper.succeed();
    }

    public static void setToMap(GameTestHelper helper) {
        var map = new EnableMap();
        assertFalse(map.enabled("quarry"));
        map.set("quarry", true);
        assertTrue(map.enabled("quarry"));
        helper.succeed();
    }
}
