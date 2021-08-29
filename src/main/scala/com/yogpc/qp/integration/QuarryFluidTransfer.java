package com.yogpc.qp.integration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import alexiil.mc.lib.attributes.AttributeSourceType;
import alexiil.mc.lib.attributes.SearchOptions;
import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.FluidAttributes;
import alexiil.mc.lib.attributes.fluid.FluidExtractable;
import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.filter.FluidFilter;
import alexiil.mc.lib.attributes.fluid.impl.EmptyFluidExtractable;
import alexiil.mc.lib.attributes.fluid.impl.RejectingFluidInsertable;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.FluidKey;
import com.yogpc.qp.machines.MachineStorage;
import com.yogpc.qp.machines.advpump.TileAdvPump;
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
    private static final List<FluidTransfer> transfers = new ArrayList<>();

    public static boolean isRegistered() {
        return registered;
    }

    @SuppressWarnings("SpellCheckingInspection")
    public static void register() {
        if (FabricLoader.getInstance().isModLoaded("libblockattributes_fluids")) {
            QuarryPlus.LOGGER.debug("Trying to register LBA fluid handler.");
            transfers.add(BCFluidRegister.getBCTransfer());
            BCFluidRegister.registerAttributes();
            registered = true;
        }
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
    public static Pair<Fluid, Long> transfer(World world, BlockPos pos, @NotNull BlockEntity destination, Fluid fluid, long amount, Direction direction) {
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
    boolean acceptable(World world, BlockPos pos, Direction direction, BlockEntity entity);

    @NotNull
    Pair<Fluid, Long> transfer(World world, BlockPos pos, @NotNull BlockEntity destination, Direction direction, long amount, Fluid fluid);
}

class BCFluidRegister {
    static void registerAttributes() {
        FluidAttributes.EXTRACTABLE.setBlockEntityAdder(AttributeSourceType.COMPAT_WRAPPER, QuarryPlus.ModObjects.QUARRY_TYPE, TileQuarry.class,
            (blockEntity, to) -> to.add(new BCExtractable(blockEntity)));
        FluidAttributes.EXTRACTABLE.setBlockEntityAdder(AttributeSourceType.INSTANCE, QuarryPlus.ModObjects.ADV_PUMP_TYPE, TileAdvPump.class,
            (blockEntity, to) -> to.add(EmptyFluidExtractable.SUPPLIER));
    }

    static FluidTransfer getBCTransfer() {
        return new BCFluidTransfer();
    }
}

class BCFluidTransfer implements FluidTransfer {

    @Override
    public boolean acceptable(World world, BlockPos pos, Direction direction, BlockEntity entity) {
        var attribute = FluidAttributes.INSERTABLE.get(world, pos, SearchOptions.inDirection(direction.getOpposite()));
        return attribute != RejectingFluidInsertable.NULL;
    }

    @Override
    @NotNull
    public Pair<Fluid, Long> transfer(World world, BlockPos pos, @NotNull BlockEntity destination, Direction direction, long amount, Fluid fluid) {
        var volume = FluidKeys.get(fluid).withAmount(FluidAmount.of(amount, 1000)); // I'm assuming one bucket is equal to 1000 mB.
        var insertable = FluidAttributes.INSERTABLE.get(world, pos, SearchOptions.inDirection(direction.getOpposite()));
        var attemptResult = insertable.attemptInsertion(volume, Simulation.SIMULATE);
        if (!attemptResult.equals(volume)) {
            // Accepted.
            var exceeded = insertable.insert(volume);
            return Pair.of(exceeded.getRawFluid(), exceeded.amount().asLong(1000L));
        } else {
            return Pair.of(fluid, amount);
        }
    }
}

class BCExtractable implements FluidExtractable {
    private final MachineStorage.HasStorage storageHolder;

    BCExtractable(MachineStorage.HasStorage hasStorage) {
        this.storageHolder = hasStorage;
    }

    @Override
    public FluidVolume attemptExtraction(FluidFilter filter, FluidAmount maxAmount, Simulation simulation) {
        var storage = this.storageHolder.getStorage();
        for (Map.Entry<FluidKey, Long> entry : storage.getFluidMap().entrySet()) {
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
class FabricFluidTransfer implements FluidTransfer {
    static void register() { // STUB for future.
    }

    @Override
    public boolean acceptable(World world, BlockPos pos, Direction direction, BlockEntity entity) {
        return FluidStorage.SIDED.find(world, pos, null, entity, direction) != null;
    }

    @Override
    public @NotNull Pair<Fluid, Long> transfer(World world, BlockPos pos, @NotNull BlockEntity destination, Direction direction, long amount, Fluid fluid) {
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
