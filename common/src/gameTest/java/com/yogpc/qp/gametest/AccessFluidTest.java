package com.yogpc.qp.gametest;

import com.google.common.base.CaseFormat;
import com.yogpc.qp.FluidStackLike;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.machine.MachineStorage;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class AccessFluidTest {
    public static Stream<TestFunction> empty(String batchName, String structureName) {
        return Stream.of(
            Items.BUCKET,
            Items.AIR,
            Items.COBBLESTONE
        ).map(i -> {
            var name = "AccessFluidTestEmpty_%s".formatted(BuiltInRegistries.ITEM.getKey(i).getPath());
            return new TestFunction(batchName, CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name), structureName, 100, 0, true, GameTestFunctions.wrapper(g -> emptyBucket(g, i)));
        });
    }

    private static void emptyBucket(GameTestHelper helper, Item item) {
        var access = PlatformAccess.getAccess();
        var fluid = access.getFluidInItem(new ItemStack(item));
        assertEquals(FluidStackLike.EMPTY, fluid);
        helper.succeed();
    }

    public static void waterBucket(GameTestHelper helper) {
        var access = PlatformAccess.getAccess();
        var fluid = access.getFluidInItem(new ItemStack(Items.WATER_BUCKET));
        var expected = new FluidStackLike(Fluids.WATER, MachineStorage.ONE_BUCKET, DataComponentPatch.EMPTY);
        assertEquals(expected, fluid);
        helper.succeed();
    }

    public static void lavaBucket(GameTestHelper helper) {
        var access = PlatformAccess.getAccess();
        var fluid = access.getFluidInItem(new ItemStack(Items.LAVA_BUCKET));
        var expected = new FluidStackLike(Fluids.LAVA, MachineStorage.ONE_BUCKET, DataComponentPatch.EMPTY);
        assertEquals(expected, fluid);
        helper.succeed();
    }
}
