package com.yogpc.qp.neoforge.gametest;

import com.kotori316.testutil.common.TestFunction;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.MachineStorage;
import com.yogpc.qp.machine.MachineStorageHolder;
import com.yogpc.qp.neoforge.machine.MachineStorageNeoForge;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ResourceHandler;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public final class MachineStorageNeoForgeTest {
    static List<TestFunction> tests() {
        var instance = new MachineStorageNeoForgeTest();
        return List.of(
            TestFunction.create(QuarryPlus.modID, "loadHandler", instance::loadHandler),
            TestFunction.create(QuarryPlus.modID, "fluidContent", instance::fluidContent)
        );
    }

    private static final MachineStorageHolder<MachineStorageHolder.Constant> ACCESSOR = new MachineStorageHolder.ForConstant();

    public void loadHandler(GameTestHelper helper) {
        var storage = MachineStorage.of();
        var handler = assertDoesNotThrow(() -> MachineStorageNeoForge.createItemHandler(ACCESSOR, new MachineStorageHolder.Constant(storage)));
        assertInstanceOf(ResourceHandler.class, handler);
        helper.succeed();
    }

    public void fluidContent(GameTestHelper helper) {
        var storage = MachineStorage.of();
        storage.addBucketFluid(new ItemStack(Items.WATER_BUCKET));
        var handler = assertDoesNotThrow(() -> MachineStorageNeoForge.createFluidHandler(ACCESSOR, new MachineStorageHolder.Constant(storage)));
        assertNotNull(handler);

        assertFalse(handler.getResource(0).isEmpty(), "Storage must have valid fluid");
        var fluid = handler.getResource(0);
        assertEquals(Fluids.WATER, fluid.getFluid());
        assertEquals(FluidType.BUCKET_VOLUME, handler.getAmountAsLong(0));

        helper.succeed();
    }
}
