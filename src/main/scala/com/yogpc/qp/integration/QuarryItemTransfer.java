package com.yogpc.qp.integration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.MachineStorage;
import com.yogpc.qp.machines.filler.FillerEntity;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QuarryItemTransfer {
    private static final List<ItemTransfer<?>> transfers = new ArrayList<>();

    public static void register() {
        if (FabricLoader.getInstance().isModLoaded("fabric-transfer-api-v1")) {
            QuarryPlus.LOGGER.debug("Trying to register fabric item transfer api.");
            FabricItemTransfer.register();
            transfers.add(FabricItemTransfer.fabricTransfer());
        }
        transfers.add(new VanillaItemTransfer());
    }

    public static ItemStack transfer(Level world, BlockPos pos, ItemStack send, Direction direction) {
        if (send.isEmpty()) {
            QuarryPlus.LOGGER.warn("Empty items are tried to be transferred from {}.", pos.relative(direction.getOpposite()));
            return ItemStack.EMPTY;
        }
        for (ItemTransfer<?> transfer : transfers) {
            ItemStack rest = transferInternal(transfer, world, pos, send, direction);
            if (send.getCount() != rest.getCount()) {
                return rest;
            }
        }
        return send;
    }

    public static boolean destinationExists(Level world, BlockPos pos, Direction direction) {
        return transfers.stream().anyMatch(t -> destinationExists(t, world, pos, direction));
    }

    private static <T> ItemStack transferInternal(ItemTransfer<T> transfer, Level world, BlockPos pos, ItemStack send, Direction direction) {
        var dest = transfer.getDestination(world, pos, direction);
        if (transfer.isValidDestination(dest)) {
            ItemStack rest = transfer.transfer(dest, send, direction);
            if (send.getCount() != rest.getCount()) {
                return rest;
            } else {
                return send; // This is fail-safe logic to avoid stack modification.
            }
        } else {
            return send;
        }
    }

    private static <T> boolean destinationExists(ItemTransfer<T> transfer, Level world, BlockPos pos, Direction direction) {
        return transfer.isValidDestination(transfer.getDestination(world, pos, direction));
    }
}

interface ItemTransfer<T> {
    @Nullable
    T getDestination(Level world, BlockPos pos, Direction direction);

    @Contract("null -> false")
    boolean isValidDestination(@Nullable T t);

    /**
     * @return stacks which were NOT transferred to the destination.
     */
    @NotNull
    ItemStack transfer(T destination, ItemStack send, Direction direction);
}

class VanillaItemTransfer implements ItemTransfer<Container> {

    @Override
    @Nullable
    public Container getDestination(Level world, BlockPos pos, Direction direction) {
        return HopperBlockEntity.getContainerAt(world, pos);
    }

    @Override
    public boolean isValidDestination(@Nullable Container inventory) {
        return inventory != null;
    }

    @Override
    public ItemStack transfer(Container destination, ItemStack send, Direction direction) {
        return HopperBlockEntity.addItem(null, destination, send, direction);
    }
}

@SuppressWarnings("UnstableApiUsage")
class FabricItemTransfer implements ItemTransfer<Storage<ItemVariant>> {
    static void register() {
        ItemStorage.SIDED.registerForBlockEntities(MachineStorage::getItemStorage,
            QuarryPlus.ModObjects.ADV_QUARRY_TYPE, QuarryPlus.ModObjects.QUARRY_TYPE);
        ItemStorage.SIDED.registerForBlockEntities(FillerEntity::getFillerStorage,
            QuarryPlus.ModObjects.FILLER_TYPE);
    }

    static ItemTransfer<?> fabricTransfer() {
        return new FabricItemTransfer();
    }

    @Override
    @Nullable
    public Storage<ItemVariant> getDestination(Level world, BlockPos pos, Direction direction) {
        var state = world.getBlockState(pos);
        if (state.isAir()) return null;
        return ItemStorage.SIDED.find(world, pos, state, null, direction);
    }

    @Override
    public boolean isValidDestination(Storage<ItemVariant> itemVariantStorage) {
        return itemVariantStorage != null && itemVariantStorage.supportsInsertion();
    }

    @Override
    public @NotNull ItemStack transfer(Storage<ItemVariant> destination, ItemStack send, Direction direction) {
        Objects.requireNonNull(destination, "The destination must be checked with a method `isValidDestination`.");
        long insertAmount;
        try (var simulationTransaction = Transaction.openOuter()) {
            insertAmount = destination.insert(ItemVariant.of(send), send.getCount(), simulationTransaction);
            simulationTransaction.abort();
        }
        if (insertAmount > 0) {
            var copy = send.copy();
            long inserted;
            try (var executionTransaction = Transaction.openOuter()) {
                inserted = destination.insert(ItemVariant.of(send), insertAmount, executionTransaction);
                executionTransaction.commit();
            }
            copy.shrink((int) inserted);
            return copy;
        } else {
            return send;
        }
    }
}
