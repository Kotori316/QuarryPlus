package com.yogpc.qp.gametest;

import com.google.common.base.CaseFormat;
import com.kotori316.testutil.common.TestFunction;
import com.yogpc.qp.FluidStackLike;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.MachineStorage;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class AccessFluidTest {
    public static Stream<TestFunction> empty(String batchName, String structureName) {
        return Stream.of(
            ResourceLocation.fromNamespaceAndPath("minecraft", "bucket"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "air"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "cobblestone")
        ).map(location -> {
            var name = "AccessFluidTestEmpty_%s".formatted(location.getPath());
            return TestFunction.createWithStructure(QuarryPlus.modID, batchName, CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name), structureName, g -> emptyBucket(g, location));
        });
    }

    private static void emptyBucket(GameTestHelper helper, ResourceLocation location) {
        var item = BuiltInRegistries.ITEM.getValue(location);
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
