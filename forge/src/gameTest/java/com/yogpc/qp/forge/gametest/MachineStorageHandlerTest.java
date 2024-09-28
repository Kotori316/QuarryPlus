package com.yogpc.qp.forge.gametest;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.forge.integration.MachineStorageHandler;
import com.yogpc.qp.machine.MachineStorage;
import com.yogpc.qp.machine.MachineStorageHolder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.items.IItemHandler;

import static com.yogpc.qp.forge.gametest.LoadTest.STRUCTURE;
import static org.junit.jupiter.api.Assertions.*;

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
