package com.yogpc.qp.fabric;

import com.yogpc.qp.FluidStackLike;
import com.yogpc.qp.FluidStackLikeUnit;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class PlatformAccessFabricFluidTest {
    @GameTest
    void testWater() {
        var access = new PlatformAccessFabric();
        var expected = new FluidStackLike(Fluids.WATER, FluidStackLikeUnit.ONE_BUCKET, DataComponentPatch.EMPTY);
        var result = access.getFluidInItem(new ItemStack(Items.WATER_BUCKET));
        assertEquals(expected, result);
    }

    @GameTest
    void testLava() {
        var access = new PlatformAccessFabric();
        var expected = new FluidStackLike(Fluids.LAVA, FluidStackLikeUnit.ONE_BUCKET, DataComponentPatch.EMPTY);
        var result = access.getFluidInItem(new ItemStack(Items.LAVA_BUCKET));
        assertEquals(expected, result);
    }

    @GameTest
    void testEmpty() {
        var access = new PlatformAccessFabric();
        var expected = FluidStackLike.EMPTY;
        var result = access.getFluidInItem(new ItemStack(Items.BUCKET));
        assertEquals(expected, result);
    }
}
