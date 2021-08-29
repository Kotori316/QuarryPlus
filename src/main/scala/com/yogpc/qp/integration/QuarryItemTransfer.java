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
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class QuarryItemTransfer {
    private static final List<ItemTransfer<?>> transfers = new ArrayList<>();

    @SuppressWarnings("SpellCheckingInspection")
    public static void register() {
        if (FabricLoader.getInstance().isModLoaded("libblockattributes_items")) {
            transfers.add(new BCItemTransfer());
            BCItemTransfer.attributeRegister();
        }
        transfers.add(new VanillaItemTransfer());
    }

    public static ItemStack transfer(World world, BlockPos pos, ItemStack send, Direction direction) {
        for (ItemTransfer<?> transfer : transfers) {
            var rest = transferInternal(transfer, world, pos, send, direction);
            if (send.getCount() != rest.getCount()) {
                return rest;
            }
        }
        return send;
    }

    public static boolean destinationExists(World world, BlockPos pos, Direction direction) {
        return transfers.stream().anyMatch(t -> destinationExists(t, world, pos, direction));
    }

    private static <T> ItemStack transferInternal(ItemTransfer<T> transfer, World world, BlockPos pos, ItemStack send, Direction direction) {
        var dest = transfer.getDestination(world, pos, direction);
        if (transfer.isValidDestination(dest)) {
            var rest = transfer.transfer(dest, send, direction);
            if (send.getCount() != rest.getCount()) {
                return rest;
            } else {
                return send; // This is fail-safe logic to avoid stack modification.
            }
        } else {
            return send;
        }
    }

    private static <T> boolean destinationExists(ItemTransfer<T> transfer, World world, BlockPos pos, Direction direction) {
        return transfer.isValidDestination(transfer.getDestination(world, pos, direction));
    }
}

interface ItemTransfer<T> {
    T getDestination(World world, BlockPos pos, Direction direction);

    boolean isValidDestination(T t);

    /**
     * @return stacks which were NOT transferred to the destination.
     */
    @NotNull
    ItemStack transfer(T destination, ItemStack send, Direction direction);
}

class VanillaItemTransfer implements ItemTransfer<Inventory> {

    @Override
    public Inventory getDestination(World world, BlockPos pos, Direction direction) {
        return HopperBlockEntity.getInventoryAt(world, pos);
    }

    @Override
    public boolean isValidDestination(Inventory inventory) {
        return inventory != null;
    }

    @Override
    public ItemStack transfer(Inventory destination, ItemStack send, Direction direction) {
        return HopperBlockEntity.transfer(null, destination, send, direction);
    }
}

class BCItemTransfer implements ItemTransfer<ItemInsertable> {

    @Override
    public ItemInsertable getDestination(World world, BlockPos pos, Direction direction) {
        return ItemAttributes.INSERTABLE.get(world, pos, SearchOptions.inDirection(direction.getOpposite()));
    }

    @Override
    public boolean isValidDestination(ItemInsertable itemInsertable) {
        return itemInsertable != ItemAttributes.INSERTABLE.defaultValue;
    }

    @Override
    public ItemStack transfer(ItemInsertable destination, ItemStack send, Direction direction) {
        var simulation = destination.attemptInsertion(send, Simulation.SIMULATE);
        if (simulation.getCount() < send.getCount()) {
            return destination.attemptInsertion(send, Simulation.ACTION);
        } else {
            return send;
        }
    }

    static void attributeRegister() {
        ItemAttributes.EXTRACTABLE.setBlockEntityAdder(AttributeSourceType.INSTANCE,
            QuarryPlus.ModObjects.QUARRY_TYPE, TileQuarry.class, (blockEntity, to) -> to.add(EmptyItemExtractable.SUPPLIER));
    }
}
