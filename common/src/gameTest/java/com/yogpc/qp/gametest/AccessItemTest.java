package com.yogpc.qp.gametest;

import com.google.common.base.CaseFormat;
import com.kotori316.testutil.common.TestFunction;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.world.item.CreativeModeTab;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;

public final class AccessItemTest {
    public static Stream<TestFunction> accessItems(String batchName, String structureName) {
        var items = PlatformAccess.getAccess().registerObjects().allItems();

        return items.map(e -> {
            var name = "AccessItemTest_%s".formatted(e.getKey().getPath());
            return TestFunction.createWithStructure(QuarryPlus.modID, batchName, CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name), structureName, g -> {
                var parameter = new CreativeModeTab.ItemDisplayParameters(g.getLevel().enabledFeatures(), false, g.getLevel().registryAccess());
                var i = e.getValue().get();
                assertAll(i.creativeTabItem(parameter).map(t -> () -> assertFalse(t.isEmpty())));
                g.succeed();
            });
        });
    }
}
