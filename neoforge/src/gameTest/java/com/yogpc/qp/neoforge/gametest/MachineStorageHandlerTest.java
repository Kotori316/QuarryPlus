package com.yogpc.qp.neoforge.gametest;

import com.kotori316.testutil.common.TestFunction;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.MachineStorage;
import com.yogpc.qp.machine.MachineStorageHolder;
import com.yogpc.qp.neoforge.integration.MachineStorageHandler;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public final class MachineStorageHandlerTest {
    static List<TestFunction> tests() {
        var instance = new MachineStorageHandlerTest();
        return List.of(
            TestFunction.create(QuarryPlus.modID, "loadHandler", instance::loadHandler),
            TestFunction.create(QuarryPlus.modID, "fluidContent", instance::fluidContent)
        );
    }

    private static final MachineStorageHolder<MachineStorageHolder.Constant> ACCESSOR = new MachineStorageHolder.ForConstant();

    public void loadHandler(GameTestHelper helper) {
        var storage = MachineStorage.of();
        var handler = assertDoesNotThrow(() -> new MachineStorageHandler<>(ACCESSOR, new MachineStorageHolder.Constant(storage)));
        assertInstanceOf(IItemHandler.class, handler);
        assertInstanceOf(IFluidHandler.class, handler);
        helper.succeed();
    }

    public void fluidContent(GameTestHelper helper) {
        var storage = MachineStorage.of();
        storage.addBucketFluid(new ItemStack(Items.WATER_BUCKET));
        var handler = assertDoesNotThrow(() -> new MachineStorageHandler<>(ACCESSOR, new MachineStorageHolder.Constant(storage)));

        assertFalse(handler.getFluidInTank(0).isEmpty(), "Storage must have valid fluid");
        var fluid = handler.getFluidInTank(0);
        assertEquals(Fluids.WATER, fluid.getFluid());
        assertEquals(FluidType.BUCKET_VOLUME, fluid.getAmount());

        helper.succeed();
    }
}
