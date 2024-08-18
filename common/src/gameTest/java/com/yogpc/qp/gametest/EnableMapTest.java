package com.yogpc.qp.gametest;

import com.google.common.base.CaseFormat;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.QpItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;

public final class EnableMapTest {

    public static Stream<TestFunction> test(String batchName, String structureName) {
        return Stream.concat(
            configured(batchName, structureName),
            hasValidBlocks(batchName, structureName)
        );
    }

    private static Stream<TestFunction> configured(String batchName, String structureName) {
        var blockEntityTypes = BuiltInRegistries.BLOCK_ENTITY_TYPE.keySet()
            .stream()
            .filter(e -> e.getNamespace().equals(QuarryPlus.modID));
        var items = BuiltInRegistries.ITEM.entrySet().stream()
            .filter(e -> e.getValue() instanceof QpItem)
            .map(Map.Entry::getKey)
            .map(ResourceKey::location);

        return Stream.concat(blockEntityTypes, items).map(e -> {
            var name = "EnableMapTest_%s".formatted(e.getPath());
            return new TestFunction(batchName, CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name), structureName, 5, 0, true, GameTestFunctions.wrapper(g -> {
                if (!PlatformAccess.getAccess().registerObjects().defaultEnableSetting().containsKey(e.getPath())) {
                    g.fail("%s is not configured".formatted(e.getPath()));
                }
                g.succeed();
            }));
        });
    }

    private static Stream<TestFunction> hasValidBlocks(String batchName, String structureName) {
        var blockEntityTypes = BuiltInRegistries.BLOCK_ENTITY_TYPE.entrySet()
            .stream()
            .filter(e -> e.getKey().location().getNamespace().equals(QuarryPlus.modID));

        return blockEntityTypes.map(e -> {
            var name = "EnableMapTestHasValidBlock_%s".formatted(e.getKey().location().getPath());
            var b = e.getValue();
            return new TestFunction(batchName, CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name), structureName, 100, 0, true, GameTestFunctions.wrapper(g -> {
                try {
                    var field = BlockEntityType.class.getDeclaredField("validBlocks");
                    field.setAccessible(true);
                    var blocks = (Set<?>) field.get(b);
                    assertFalse(blocks.isEmpty());
                    g.succeed();
                } catch (ReflectiveOperationException exception) {
                    throw new RuntimeException(exception);
                }
            }));
        });
    }
}
