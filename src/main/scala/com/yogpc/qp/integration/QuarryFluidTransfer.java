package com.yogpc.qp.integration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import alexiil.mc.lib.attributes.AttributeSourceType;
import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.FluidAttributes;
import alexiil.mc.lib.attributes.fluid.FluidExtractable;
import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.filter.FluidFilter;
import alexiil.mc.lib.attributes.fluid.impl.RejectingFluidInsertable;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.MachineStorage;
import com.yogpc.qp.machines.quarry.TileQuarry;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

public class QuarryFluidTransfer {
    private static boolean registered;
    private static final List<Transfer> transfers = new ArrayList<>();

    public static boolean isRegistered() {
        return registered;
    }

    @SuppressWarnings("SpellCheckingInspection")
    public static void register() {
        if (FabricLoader.getInstance().isModLoaded("libblockattributes_fluids")) {
            QuarryPlus.LOGGER.debug("Trying to register LBA fluid handler.");
            transfers.add(BCRegister.getBCTransfer());
            BCRegister.registerAttributes();
            registered = true;
        }
        if (FabricLoader.getInstance().isModLoaded("fabric-transfer-api-v1")) {
            QuarryPlus.LOGGER.debug("Trying to register fabric fluid transfer api.");
            FabricTransfer.register();
            transfers.add(new FabricTransfer());
            registered = true;
        }
    }

    /**
     * @return a pair of fluid and amount which were NOT transferred. The excess amount.
     */
    public static Pair<Fluid, Long> transfer(World world, BlockPos pos, @NotNull BlockEntity destination, Fluid fluid, long amount, Direction direction) {
        if (!registered) return Pair.of(fluid, amount);

        for (Transfer transfer : transfers) {
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

interface Transfer {
    boolean acceptable(World world, BlockPos pos, Direction direction, BlockEntity entity);

    @NotNull
    Pair<Fluid, Long> transfer(World world, BlockPos pos, @NotNull BlockEntity destination, Direction direction, long amount, Fluid fluid);
}

class BCRegister {
    static void registerAttributes() {
        FluidAttributes.EXTRACTABLE.setBlockEntityAdder(AttributeSourceType.COMPAT_WRAPPER, QuarryPlus.ModObjects.QUARRY_TYPE, TileQuarry.class,
            (blockEntity, to) -> to.add(new BCExtractable(blockEntity)));
    }

    static Transfer getBCTransfer() {
        return new BCTransfer();
    }
}

class BCTransfer implements Transfer {

    @Override
    public boolean acceptable(World world, BlockPos pos, Direction direction, BlockEntity entity) {
        var attribute = FluidAttributes.INSERTABLE.get(world, pos);
        return attribute != RejectingFluidInsertable.NULL;
    }

    @Override
    @NotNull
    public Pair<Fluid, Long> transfer(World world, BlockPos pos, @NotNull BlockEntity destination, Direction direction, long amount, Fluid fluid) {
        var volume = FluidKeys.get(fluid).withAmount(FluidAmount.of(amount, 1000)); // I'm assuming one bucket is equal to 1000 mB.
        var insertable = FluidAttributes.INSERTABLE.get(world, pos);
        var attemptResult = insertable.attemptInsertion(volume, Simulation.SIMULATE);
        if (!attemptResult.equals(insertable)) {
            // Accepted.
            var exceeded = insertable.insert(volume);
            return Pair.of(exceeded.getRawFluid(), exceeded.amount().asLong(1000L));
        } else {
            return Pair.of(fluid, amount);
        }
    }
}

class BCExtractable implements FluidExtractable {
    private final MachineStorage storage;

    BCExtractable(MachineStorage.HasStorage hasStorage) {
        this.storage = hasStorage.getStorage();
    }

    @Override
    public FluidVolume attemptExtraction(FluidFilter filter, FluidAmount maxAmount, Simulation simulation) {
        for (Map.Entry<MachineStorage.FluidKey, Long> entry : storage.getFluidMap().entrySet()) {
            var fluidKey = FluidKeys.get(entry.getKey().fluid());
            if (filter.matches(fluidKey)) {
                var extractAmount = maxAmount.min(FluidAmount.of(entry.getValue(), 1000));
                if (simulation.isAction()) {
                    storage.addFluid(entry.getKey().fluid(), -extractAmount.asLong(1000));
                }
                return fluidKey.withAmount(extractAmount);
            }
        }
        return FluidVolumeUtil.EMPTY;
    }
}

@SuppressWarnings({"deprecation", "UnstableApiUsage"})
class FabricTransfer implements Transfer {
    static void register() { // STUB for future.
    }

    @Override
    public boolean acceptable(World world, BlockPos pos, Direction direction, BlockEntity entity) {
        return FluidStorage.SIDED.find(world, pos, direction) != null;
    }

    @Override
    public @NotNull Pair<Fluid, Long> transfer(World world, BlockPos pos, @NotNull BlockEntity destination, Direction direction, long amount, Fluid fluid) {
        var storage = FluidStorage.SIDED.find(world, pos, direction);
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
