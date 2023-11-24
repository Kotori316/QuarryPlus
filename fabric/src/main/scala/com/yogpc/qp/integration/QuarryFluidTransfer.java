package com.yogpc.qp.integration;

import java.util.ArrayList;
import java.util.List;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.MachineStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

public class QuarryFluidTransfer {
    private static boolean registered;
    private static final List<FluidTransfer> transfers = new ArrayList<>();

    public static boolean isRegistered() {
        return registered;
    }

    public static void register() {
        if (FabricLoader.getInstance().isModLoaded("fabric-transfer-api-v1")) {
            QuarryPlus.LOGGER.debug("Trying to register fabric fluid transfer api.");
            FabricFluidTransfer.register();
            transfers.add(new FabricFluidTransfer());
            registered = true;
        }
    }

    /**
     * @return A pair of fluid and amount which were NOT transferred. The excess amount.
     */
    public static Pair<Fluid, Long> transfer(Level world, BlockPos pos, @NotNull BlockEntity destination, Fluid fluid, long amount, Direction direction) {
        if (!registered) return Pair.of(fluid, amount);

        for (FluidTransfer transfer : transfers) {
            if (transfer.acceptable(world, pos, direction, destination)) {
                var excess = transfer.transfer(world, pos, destination, direction, amount, fluid);
                if (excess.getRight() == 0) {
                    // Early return
                    return excess;
                }
                fluid = excess.getLeft();
                amount = excess.getRight();
            }
        }
        return Pair.of(fluid, amount);
    }
}

interface FluidTransfer {
    boolean acceptable(Level world, BlockPos pos, Direction direction, BlockEntity entity);

    @NotNull
    Pair<Fluid, Long> transfer(Level world, BlockPos pos, @NotNull BlockEntity destination, Direction direction, long amount, Fluid fluid);
}


@SuppressWarnings({"UnstableApiUsage"})
class FabricFluidTransfer implements FluidTransfer {
    static void register() {
        FluidStorage.SIDED.registerForBlockEntities(MachineStorage::getFluidStorage,
            QuarryPlus.ModObjects.ADV_QUARRY_TYPE, QuarryPlus.ModObjects.QUARRY_TYPE, QuarryPlus.ModObjects.ADV_PUMP_TYPE);
    }

    @Override
    public boolean acceptable(Level world, BlockPos pos, Direction direction, BlockEntity entity) {
        return FluidStorage.SIDED.find(world, pos, null, entity, direction) != null;
    }

    @Override
    public @NotNull Pair<Fluid, Long> transfer(Level world, BlockPos pos, @NotNull BlockEntity destination, Direction direction, long amount, Fluid fluid) {
        var storage = FluidStorage.SIDED.find(world, pos, null, destination, direction);
        if (storage == null) {
            // Not fluid container.
            return Pair.of(fluid, amount);
        }
        long insertAmount; // In fabric unit
        try (Transaction simulationTransaction = Transaction.openOuter()) {
            insertAmount = storage.insert(FluidVariant.of(fluid), amount / MachineStorage.ONE_BUCKET * FluidConstants.BUCKET, simulationTransaction);
            simulationTransaction.abort();
        }
        if (insertAmount > 0) {
            long inserted; // In mB unit
            try (Transaction executionTransaction = Transaction.openOuter()) {
                inserted = storage.insert(FluidVariant.of(fluid), insertAmount, executionTransaction) / FluidConstants.BUCKET * MachineStorage.ONE_BUCKET;
                executionTransaction.commit();
            }
            return Pair.of(fluid, amount - inserted);
        } else {
            // No fluid is inserted.
            return Pair.of(fluid, amount);
        }
    }
}
