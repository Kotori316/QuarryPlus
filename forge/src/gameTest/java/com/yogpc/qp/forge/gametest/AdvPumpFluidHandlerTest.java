package com.yogpc.qp.forge.gametest;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.data.GatherGameTest;
import com.yogpc.qp.forge.integration.AdvPumpFluidHandler;
import com.yogpc.qp.machine.MachineStorage;
import com.yogpc.qp.machine.MachineStorageHolder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTest;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public final class AdvPumpFluidHandlerTest implements GatherGameTest {
    private static final MachineStorageHolder<MachineStorageHolder.Constant> ACCESSOR = new MachineStorageHolder.ForConstant();

    @GameTest()
    public void loadHandler(GameTestHelper helper) {
        var storage = MachineStorage.of();
        var handler = assertDoesNotThrow(() -> new AdvPumpFluidHandler<>(ACCESSOR, new MachineStorageHolder.Constant(storage)));
        assertInstanceOf(IFluidHandler.class, handler);
        helper.succeed();
    }

    @GameTest()
    public void tankCountMatchesDistinctFluidsHeld(GameTestHelper helper) {
        var storage = MachineStorage.of();
        var handler = new AdvPumpFluidHandler<>(ACCESSOR, new MachineStorageHolder.Constant(storage));
        assertEquals(0, handler.getTanks(), "No fluid held yet -> no tanks, no reserved empty slot for insertion");

        storage.addBucketFluid(new ItemStack(Items.WATER_BUCKET));
        assertEquals(1, handler.getTanks(), "Exactly one tank per distinct fluid held, no +1 for insertion since insertion is refused");

        helper.succeed();
    }

    @GameTest()
    public void isFluidValidAlwaysFalse(GameTestHelper helper) {
        var storage = MachineStorage.of();
        var handler = new AdvPumpFluidHandler<>(ACCESSOR, new MachineStorageHolder.Constant(storage));
        assertFalse(handler.isFluidValid(0, new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME)));
        helper.succeed();
    }

    @GameTest()
    public void insertionIsRefused(GameTestHelper helper) {
        var storage = MachineStorage.of();
        var handler = new AdvPumpFluidHandler<>(ACCESSOR, new MachineStorageHolder.Constant(storage));

        var filled = handler.fill(new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME), IFluidHandler.FluidAction.EXECUTE);

        assertEquals(0, filled, "fill() must always report that nothing was accepted");
        assertEquals(0, storage.getFluidCount(Fluids.WATER), "Nothing should have actually been added to storage");
        helper.succeed();
    }

    @GameTest()
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

    @GameTest()
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

    @Override
    public Collection<GameTestProperty> gather() {
        var testData = GameTestProperty.empty();
        var prefix = "adv_pump_fluid_handler";
        return List.of(
            new GameTestProperty(Identifier.fromNamespaceAndPath(QuarryPlus.modID, prefix + "_load_handler"), testData, this::loadHandler),
            new GameTestProperty(Identifier.fromNamespaceAndPath(QuarryPlus.modID, prefix + "_tank_count_matches_distinct_fluids_held"), testData, this::tankCountMatchesDistinctFluidsHeld),
            new GameTestProperty(Identifier.fromNamespaceAndPath(QuarryPlus.modID, prefix + "_is_fluid_valid_always_false"), testData, this::isFluidValidAlwaysFalse),
            new GameTestProperty(Identifier.fromNamespaceAndPath(QuarryPlus.modID, prefix + "_insertion_is_refused"), testData, this::insertionIsRefused),
            new GameTestProperty(Identifier.fromNamespaceAndPath(QuarryPlus.modID, prefix + "_extraction_is_allowed"), testData, this::extractionIsAllowed),
            new GameTestProperty(Identifier.fromNamespaceAndPath(QuarryPlus.modID, prefix + "_extraction_by_index_is_allowed"), testData, this::extractionByIndexIsAllowed)
        );
    }
}
