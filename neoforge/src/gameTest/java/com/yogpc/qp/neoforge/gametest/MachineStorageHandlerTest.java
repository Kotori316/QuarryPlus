package com.yogpc.qp.neoforge.gametest;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.MachineStorage;
import com.yogpc.qp.machine.MachineStorageHolder;
import com.yogpc.qp.neoforge.integration.MachineStorageHandler;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
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
    public void loadHandler(GameTestHelper helper) {
        var storage = MachineStorage.of();
        var handler = assertDoesNotThrow(() -> new MachineStorageHandler<>(ACCESSOR, new MachineStorageHolder.Constant(storage)));
        assertInstanceOf(IItemHandler.class, handler);
        assertInstanceOf(IFluidHandler.class, handler);
        helper.succeed();
    }

    @GameTest(template = STRUCTURE)
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
