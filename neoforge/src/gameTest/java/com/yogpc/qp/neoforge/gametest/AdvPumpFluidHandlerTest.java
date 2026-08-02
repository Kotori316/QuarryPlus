package com.yogpc.qp.neoforge.gametest;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.MachineStorage;
import com.yogpc.qp.machine.MachineStorageHolder;
import com.yogpc.qp.neoforge.integration.AdvPumpFluidHandler;
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

import static com.yogpc.qp.neoforge.gametest.LoadTest.STRUCTURE;
import static org.junit.jupiter.api.Assertions.*;

@PrefixGameTestTemplate(value = false)
@GameTestHolder(QuarryPlus.modID)
public final class AdvPumpFluidHandlerTest {
    private static final MachineStorageHolder<MachineStorageHolder.Constant> ACCESSOR = new MachineStorageHolder.ForConstant();

    @GameTest(template = STRUCTURE)
    public void loadHandler(GameTestHelper helper) {
        var storage = MachineStorage.of();
        var handler = assertDoesNotThrow(() -> new AdvPumpFluidHandler<>(ACCESSOR, new MachineStorageHolder.Constant(storage)));
        assertInstanceOf(IFluidHandler.class, handler);
        helper.succeed();
    }

    @GameTest(template = STRUCTURE)
    public void tankCountMatchesDistinctFluidsHeld(GameTestHelper helper) {
        var storage = MachineStorage.of();
        var handler = new AdvPumpFluidHandler<>(ACCESSOR, new MachineStorageHolder.Constant(storage));
        assertEquals(0, handler.getTanks(), "No fluid held yet -> no tanks, no reserved empty slot for insertion");

        storage.addBucketFluid(new ItemStack(Items.WATER_BUCKET));
        assertEquals(1, handler.getTanks(), "Exactly one tank per distinct fluid held, no +1 for insertion since insertion is refused");

        helper.succeed();
    }

    @GameTest(template = STRUCTURE)
    public void isFluidValidAlwaysFalse(GameTestHelper helper) {
        var storage = MachineStorage.of();
        var handler = new AdvPumpFluidHandler<>(ACCESSOR, new MachineStorageHolder.Constant(storage));
        assertFalse(handler.isFluidValid(0, new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME)));
        helper.succeed();
    }

    @GameTest(template = STRUCTURE)
    public void insertionIsRefused(GameTestHelper helper) {
        var storage = MachineStorage.of();
        var handler = new AdvPumpFluidHandler<>(ACCESSOR, new MachineStorageHolder.Constant(storage));

        var filled = handler.fill(new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME), IFluidHandler.FluidAction.EXECUTE);

        assertEquals(0, filled, "fill() must always report that nothing was accepted");
        assertEquals(0, storage.getFluidCount(Fluids.WATER), "Nothing should have actually been added to storage");
        helper.succeed();
    }

    @GameTest(template = STRUCTURE)
    public void extractionIsAllowed(GameTestHelper helper) {
        var storage = MachineStorage.of();
        storage.addBucketFluid(new ItemStack(Items.WATER_BUCKET));
        var handler = new AdvPumpFluidHandler<>(ACCESSOR, new MachineStorageHolder.Constant(storage));

        var drained = handler.drain(new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME), IFluidHandler.FluidAction.EXECUTE);

        assertEquals(Fluids.WATER, drained.getFluid());
        assertEquals(FluidType.BUCKET_VOLUME, drained.getAmount());
        assertEquals(0, storage.getFluidCount(Fluids.WATER), "The extracted fluid must actually leave storage");
        helper.succeed();
    }

    @GameTest(template = STRUCTURE)
    public void extractionByIndexIsAllowed(GameTestHelper helper) {
        var storage = MachineStorage.of();
        storage.addBucketFluid(new ItemStack(Items.WATER_BUCKET));
        var handler = new AdvPumpFluidHandler<>(ACCESSOR, new MachineStorageHolder.Constant(storage));

        var drained = handler.drain(FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.EXECUTE);

        assertEquals(Fluids.WATER, drained.getFluid());
        assertEquals(FluidType.BUCKET_VOLUME, drained.getAmount());
        assertEquals(0, storage.getFluidCount(Fluids.WATER));
        helper.succeed();
    }
}
