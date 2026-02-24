package com.yogpc.qp.forge.gametest;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.forge.data.GatherGameTest;
import com.yogpc.qp.forge.integration.MachineStorageHandler;
import com.yogpc.qp.machine.MachineStorage;
import com.yogpc.qp.machine.MachineStorageHolder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTest;
import net.minecraftforge.items.IItemHandler;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public final class MachineStorageHandlerTest implements GatherGameTest {
    private static final MachineStorageHolder<MachineStorageHolder.Constant> ACCESSOR = new MachineStorageHolder.ForConstant();

    @GameTest()
    public void loadHandler(GameTestHelper helper) {
        var storage = MachineStorage.of();
        var handler = assertDoesNotThrow(() -> new MachineStorageHandler<>(ACCESSOR, new MachineStorageHolder.Constant(storage)));
        assertInstanceOf(IItemHandler.class, handler);
        assertInstanceOf(IFluidHandler.class, handler);
        helper.succeed();
    }

    @GameTest()
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

    @Override
    public Collection<GameTestProperty> gather() {
        var testData = GameTestProperty.empty();
        var prefix = "machine_storage_handler";
        return List.of(
            new GameTestProperty(Identifier.fromNamespaceAndPath(QuarryPlus.modID, prefix + "_load_handler"), testData, this::loadHandler),
            new GameTestProperty(Identifier.fromNamespaceAndPath(QuarryPlus.modID, prefix + "_fluid_content"), testData, this::fluidContent)
        );
    }
}
