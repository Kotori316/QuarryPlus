package com.yogpc.qp.gametest;

import com.google.common.base.CaseFormat;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.config.EnableMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.io.InputStreamReader;
import java.util.Objects;
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
        var defaultConfig = GsonHelper.parse(new InputStreamReader(
            Objects.requireNonNull(EnableMap.class.getResourceAsStream("/machine_default.json"), "Content in Jar must not be absent.")
        ));

        return blockEntityTypes.map(e -> {
            var name = "EnableMapTest_%s".formatted(e.getPath());
            return new TestFunction(batchName, CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name), structureName, 100, 0, true, GameTestFunctions.wrapper(g -> {
                if (EnableMap.ALWAYS_ON.contains(e.getPath())) {
                    g.succeed();
                    return;
                }
                if (defaultConfig.has(e.getPath())) {
                    g.succeed();
                    return;
                }
                g.fail("%s is not configured".formatted(e.getPath()));
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
