package com.yogpc.qp.integration;

import java.util.ArrayList;
import java.util.List;

import alexiil.mc.lib.attributes.AttributeSourceType;
import alexiil.mc.lib.attributes.SearchOptions;
import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.item.ItemAttributes;
import alexiil.mc.lib.attributes.item.ItemInsertable;
import alexiil.mc.lib.attributes.item.impl.EmptyItemExtractable;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.quarry.TileQuarry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.jetbrains.annotations.NotNull;

public class QuarryItemTransfer {
    private static final List<ItemTransfer<?>> transfers = new ArrayList<>();

    @SuppressWarnings("SpellCheckingInspection")
    public static void register() {
        if (FabricLoader.getInstance().isModLoaded("libblockattributes_items")) {
            transfers.add(BCItemRegister.bcTransfer());
            BCItemRegister.registerAttributes();
        }
        transfers.add(new VanillaItemTransfer());
    }

    public static ItemStack transfer(Level world, BlockPos pos, ItemStack send, Direction direction) {
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
    T getDestination(Level world, BlockPos pos, Direction direction);

    boolean isValidDestination(T t);

    /**
     * @return stacks which were NOT transferred to the destination.
     */
    @NotNull
    ItemStack transfer(T destination, ItemStack send, Direction direction);
}

class VanillaItemTransfer implements ItemTransfer<Container> {

    @Override
    public Container getDestination(Level world, BlockPos pos, Direction direction) {
        return HopperBlockEntity.getContainerAt(world, pos);
    }

    @Override
    public boolean isValidDestination(Container inventory) {
        return inventory != null;
    }

    @Override
    public ItemStack transfer(Container destination, ItemStack send, Direction direction) {
        return HopperBlockEntity.addItem(null, destination, send, direction);
    }
}

class BCItemRegister {
    static void registerAttributes() {
        ItemAttributes.EXTRACTABLE.setBlockEntityAdder(AttributeSourceType.INSTANCE,
            QuarryPlus.ModObjects.QUARRY_TYPE, TileQuarry.class, (blockEntity, to) -> to.add(EmptyItemExtractable.SUPPLIER));
    }

    static ItemTransfer<?> bcTransfer() {
        return new BCItemTransfer();
    }
}

class BCItemTransfer implements ItemTransfer<ItemInsertable> {

    @Override
    public ItemInsertable getDestination(Level world, BlockPos pos, Direction direction) {
        return ItemAttributes.INSERTABLE.get(world, pos, SearchOptions.inDirection(direction.getOpposite()));
    }

    @Override
    public boolean isValidDestination(ItemInsertable itemInsertable) {
        return itemInsertable != ItemAttributes.INSERTABLE.defaultValue;
    }

    @Override
    public ItemStack transfer(ItemInsertable destination, ItemStack send, Direction direction) {
        ItemStack simulation = destination.attemptInsertion(send, Simulation.SIMULATE);
        if (simulation.getCount() < send.getCount()) {
            return destination.attemptInsertion(send, Simulation.ACTION);
        } else {
            return send;
        }
    }
}
