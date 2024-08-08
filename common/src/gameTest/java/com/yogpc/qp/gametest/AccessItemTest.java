package com.yogpc.qp.gametest;

import com.google.common.base.CaseFormat;
import com.yogpc.qp.PlatformAccess;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.item.CreativeModeTab;

import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;

public final class AccessItemTest {
    public static Stream<TestFunction> accessItems(String batchName, String structureName) {
        var items = PlatformAccess.getAccess().registerObjects().allItems();

        return items.map(Supplier::get).map(i -> {
            var name = "AccessItemTest%s".formatted(i.getClass().getSimpleName());
            return new TestFunction(batchName, CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name), structureName, 100, 0, true, GameTestFunctions.wrapper(g -> {
                var parameter = new CreativeModeTab.ItemDisplayParameters(g.getLevel().enabledFeatures(), false, g.getLevel().registryAccess());
                assertAll(i.creativeTabItem(parameter).map(t -> () -> assertFalse(t.isEmpty())));
                g.succeed();
            }));
        });
    }
}
