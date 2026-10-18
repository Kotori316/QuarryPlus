package com.yogpc.qp.neoforge.gametest;

import com.kotori316.testutil.common.TestFunction;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.MachineStorage;
import com.yogpc.qp.machine.MachineStorageHolder;
import com.yogpc.qp.neoforge.integration.AdvPumpFluidHandler;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public final class AdvPumpFluidHandlerTest {
    static List<TestFunction> tests() {
        var instance = new AdvPumpFluidHandlerTest();
        return List.of(
            TestFunction.create(QuarryPlus.modID, "AdvPumpFluidHandlerTestLoadHandler", instance::loadHandler),
            TestFunction.create(QuarryPlus.modID, "AdvPumpFluidHandlerTestInsertionIsRefused", instance::insertionIsRefused),
            TestFunction.create(QuarryPlus.modID, "AdvPumpFluidHandlerTestExtractionIsAllowed", instance::extractionIsAllowed),
            TestFunction.create(QuarryPlus.modID, "AdvPumpFluidHandlerTestExtractionRevertsWithoutCommit", instance::extractionRevertsWithoutCommit)
        );
    }

    private static final MachineStorageHolder<MachineStorageHolder.Constant> ACCESSOR = new MachineStorageHolder.ForConstant();

    public void loadHandler(GameTestHelper helper) {
        var storage = MachineStorage.of();
        var handler = assertDoesNotThrow(() -> new AdvPumpFluidHandler<>(ACCESSOR, new MachineStorageHolder.Constant(storage)));
        assertInstanceOf(ResourceHandler.class, handler);
        helper.succeed();
    }

    public void insertionIsRefused(GameTestHelper helper) {
        var storage = MachineStorage.of();
        var handler = new AdvPumpFluidHandler<>(ACCESSOR, new MachineStorageHolder.Constant(storage));

        int inserted;
        try (var tx = Transaction.openRoot()) {
            inserted = handler.insert(FluidResource.of(Fluids.WATER), FluidType.BUCKET_VOLUME, tx);
            tx.commit();
        }

        assertEquals(0, inserted, "insert() must always report that nothing was accepted");
        assertEquals(0, storage.getFluidCount(Fluids.WATER), "Nothing should have actually been added to storage");
        helper.succeed();
    }

    public void extractionIsAllowed(GameTestHelper helper) {
        var storage = MachineStorage.of();
        storage.addBucketFluid(new ItemStack(Items.WATER_BUCKET));
        var handler = new AdvPumpFluidHandler<>(ACCESSOR, new MachineStorageHolder.Constant(storage));

        int extracted;
        try (var tx = Transaction.openRoot()) {
            extracted = handler.extract(FluidResource.of(Fluids.WATER), FluidType.BUCKET_VOLUME, tx);
            tx.commit();
        }

        assertEquals(FluidType.BUCKET_VOLUME, extracted, "The full bucket's worth must be extractable in one call");
        assertEquals(0, storage.getFluidCount(Fluids.WATER), "The extracted fluid must actually leave storage");
        helper.succeed();
    }

    public void extractionRevertsWithoutCommit(GameTestHelper helper) {
        var storage = MachineStorage.of();
        storage.addBucketFluid(new ItemStack(Items.WATER_BUCKET));
        var handler = new AdvPumpFluidHandler<>(ACCESSOR, new MachineStorageHolder.Constant(storage));

        try (var tx = Transaction.openRoot()) {
            handler.extract(FluidResource.of(Fluids.WATER), FluidType.BUCKET_VOLUME, tx);
            // Not committed -> the extraction must be rolled back
        }

        assertEquals(MachineStorage.ONE_BUCKET, storage.getFluidCount(Fluids.WATER), "Without commit, the extraction must be reverted");
        helper.succeed();
    }
}
