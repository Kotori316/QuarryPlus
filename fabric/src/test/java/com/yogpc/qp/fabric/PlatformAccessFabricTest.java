package com.yogpc.qp.fabric;

import com.yogpc.qp.BeforeMC;
import com.yogpc.qp.FluidStackLike;
import com.yogpc.qp.machine.MachineStorage;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlatformAccessFabricTest extends BeforeMC {
    PlatformAccessFabric access = new PlatformAccessFabric();

    @Nested
    class FluidTest {
        @Test
        void testWater() {
            var expected = new FluidStackLike(Fluids.WATER, MachineStorage.ONE_BUCKET, DataComponentPatch.EMPTY);
            var result = access.getFluidInItem(new ItemStack(Items.WATER_BUCKET));
            assertEquals(expected, result);
        }

        @Test
        void testLava() {
            var expected = new FluidStackLike(Fluids.LAVA, MachineStorage.ONE_BUCKET, DataComponentPatch.EMPTY);
            var result = access.getFluidInItem(new ItemStack(Items.LAVA_BUCKET));
            assertEquals(expected, result);
        }

        @Test
        void testEmpty() {
            var expected = FluidStackLike.EMPTY;
            var result = access.getFluidInItem(new ItemStack(Items.BUCKET));
            assertEquals(expected, result);
        }
    }
}
