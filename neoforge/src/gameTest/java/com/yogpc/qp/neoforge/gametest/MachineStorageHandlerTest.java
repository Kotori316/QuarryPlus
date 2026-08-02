package com.yogpc.qp.neoforge.gametest;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.MachineStorage;
import com.yogpc.qp.machine.MachineStorageHolder;
import com.yogpc.qp.neoforge.integration.MachineStorageHandler;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;

import static com.yogpc.qp.neoforge.gametest.LoadTest.STRUCTURE;
import static org.junit.jupiter.api.Assertions.*;

@PrefixGameTestTemplate(value = false)
@GameTestHolder(QuarryPlus.modID)
public final class MachineStorageHandlerTest {
    private static final MachineStorageHolder<MachineStorageHolder.Constant> ACCESSOR = new MachineStorageHolder.ForConstant();

    @GameTest(template = STRUCTURE)
    public void loadMachineStorageHolder(GameTestHelper helper) {
        var storage = MachineStorage.of();
        var handler = assertDoesNotThrow(() -> new MachineStorageHandler<>(ACCESSOR, new MachineStorageHolder.Constant(storage)));
        assertInstanceOf(IItemHandler.class, handler);
        assertInstanceOf(IFluidHandler.class, handler);
        helper.succeed();
    }

    @GameTest(template = STRUCTURE)
    public void machineStorageHandlerFluidContent(GameTestHelper helper) {
        var storage = MachineStorage.of();
        storage.addBucketFluid(new ItemStack(Items.WATER_BUCKET));
        var handler = assertDoesNotThrow(() -> new MachineStorageHandler<>(ACCESSOR, new MachineStorageHolder.Constant(storage)));

        assertFalse(handler.getFluidInTank(0).isEmpty(), "Storage must have valid fluid");
        var fluid = handler.getFluidInTank(0);
        assertEquals(Fluids.WATER, fluid.getFluid());
        assertEquals(FluidType.BUCKET_VOLUME, fluid.getAmount());

        helper.succeed();
    }

    @GameTest(template = STRUCTURE)
    public void machineStorageHandlerExtractItemSimulateThenExecute(GameTestHelper helper) {
        var storage = MachineStorage.of();
        storage.addItem(new ItemStack(Items.DIAMOND, 64));
        var handler = new MachineStorageHandler<>(ACCESSOR, new MachineStorageHolder.Constant(storage));
        var patch = DataComponentPatch.EMPTY;

        var simulated = handler.extractItem(0, 32, true);
        assertEquals(32, simulated.getCount(), "Simulate must return the would-be extracted stack");
        assertEquals(64, storage.getItemCount(Items.DIAMOND, patch), "Simulate must not modify storage");

        var extracted = handler.extractItem(0, 32, false);
        assertEquals(32, extracted.getCount(), "Must extract the requested amount");
        assertEquals(32, storage.getItemCount(Items.DIAMOND, patch), "Storage must retain the remainder after partial extraction");

        helper.succeed();
    }

    @GameTest(template = STRUCTURE)
    public void machineStorageHandlerDrainFluidSimulateThenExecute(GameTestHelper helper) {
        var storage = MachineStorage.of();
        storage.addBucketFluid(new ItemStack(Items.WATER_BUCKET));
        var handler = new MachineStorageHandler<>(ACCESSOR, new MachineStorageHolder.Constant(storage));
        var waterStack = new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME);

        var simulated = handler.drain(waterStack, IFluidHandler.FluidAction.SIMULATE);
        assertEquals(FluidType.BUCKET_VOLUME, simulated.getAmount(), "Simulate must return the would-be drained amount");
        assertEquals(FluidType.BUCKET_VOLUME, handler.getFluidInTank(0).getAmount(), "Simulate must not modify storage");

        var drained = handler.drain(waterStack, IFluidHandler.FluidAction.EXECUTE);
        assertEquals(FluidType.BUCKET_VOLUME, drained.getAmount(), "Must drain the full amount");
        assertEquals(0, storage.getFluidCount(Fluids.WATER), "Storage must be empty after full drain");

        helper.succeed();
    }
}
