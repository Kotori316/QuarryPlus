package com.yogpc.qp.machines;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.collect.Iterators;
import com.yogpc.qp.utils.MapMulti;
import javax.annotation.Nonnull;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.tuple.Pair;

public class MachineStorage {
    private Map<ItemKey, Long> itemMap = new LinkedHashMap<>();
    private Map<FluidKey, Long> fluidMap = new LinkedHashMap<>();
    protected LazyOptional<IItemHandler> itemHandler = LazyOptional.of(StorageItemHandler::new);

    public void addItem(ItemStack stack) {
        if (stack.isEmpty()) return; // No need to store empty item.
        var key = new ItemKey(stack);
        itemMap.merge(key, (long) stack.getCount(), Long::sum);
    }

    public void addFluid(ItemStack bucketItem) {
        if (bucketItem.getItem() instanceof BucketItem bucket) {
            var key = getFluidInBucket(bucket);
            fluidMap.merge(key, (long) FluidAttributes.BUCKET_VOLUME, Long::sum);
        }
    }

    public void addFluid(Fluid fluid, long amount) {
        var key = new FluidKey(fluid, null);
        fluidMap.merge(key, amount, (l1, l2) -> {
                long a = l1 + l2;
                if (a > 0) return a;
                else return null;
            }
        );
    }

    public CompoundTag toNbt() {
        var tag = new CompoundTag();
        var itemTag = new ListTag();
        itemMap.forEach((itemKey, count) -> itemTag.add(itemKey.createNbt(count)));
        var fluidTag = new ListTag();
        fluidMap.forEach((fluidKey, amount) -> fluidTag.add(fluidKey.createNbt(amount)));
        tag.put("items", itemTag);
        tag.put("fluids", fluidTag);
        return tag;
    }

    public void readNbt(CompoundTag tag) {
        var itemTag = tag.getList("items", Tag.TAG_COMPOUND);
        itemMap = itemTag.stream()
            .mapMulti(MapMulti.cast(CompoundTag.class))
            .map(n -> Pair.of(ItemKey.fromNbt(n), n.getLong("count")))
            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
        var fluidTag = tag.getList("fluids", Tag.TAG_COMPOUND);
        fluidMap = fluidTag.stream()
            .mapMulti(MapMulti.cast(CompoundTag.class))
            .map(n -> Pair.of(FluidKey.fromNbt(n), n.getLong("amount")))
            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    public Map<FluidKey, Long> getFluidMap() {
        return Map.copyOf(fluidMap); // Return copy to avoid ConcurrentModificationException
    }

    private void putFluid(FluidKey key, long amount) {
        if (amount <= 0) {
            fluidMap.remove(key);
        } else {
            fluidMap.put(key, amount);
        }
    }

    private static FluidKey getFluidInBucket(BucketItem bucket) {
        return new FluidKey(bucket.getFluid(), null);
    }

    public interface HasStorage {
        MachineStorage getStorage();
    }

    private static final int MAX_TRANSFER = 4;

    public static <T extends BlockEntity & HasStorage> BlockEntityTicker<T> passItems() {
        return (world, pos, state, blockEntity) -> {
            var storage = blockEntity.getStorage();
            int count = 0;
            for (var direction : Direction.values()) {
                var destination = Optional.ofNullable(world.getBlockEntity(pos.relative(direction)));
                var optional = destination.flatMap(d -> d.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite()).resolve());
                if (optional.isPresent()) {
                    var handler = optional.get();
                    var itemMap = new ArrayList<>(storage.itemMap.entrySet());
                    for (Map.Entry<ItemKey, Long> entry : itemMap) {
                        long beforeCount = entry.getValue();
                        boolean flag = true;
                        while (beforeCount > 0 && flag) {
                            int itemCount = (int) Math.min(entry.getKey().toStack(1).getMaxStackSize(), beforeCount);
                            var rest = ItemHandlerHelper.insertItem(handler, entry.getKey().toStack(itemCount), false);
                            if (itemCount != rest.getCount()) {
                                // Item transferred.
                                long remain = beforeCount - (itemCount - rest.getCount());
                                beforeCount = remain;
                                if (remain > 0) {
                                    // the item still exists.
                                    storage.itemMap.put(entry.getKey(), remain);
                                } else {
                                    // the items all have been transferred.
                                    storage.itemMap.remove(entry.getKey());
                                }

                                count += 1;
                                if (count >= MAX_TRANSFER) return;
                            } else {
                                flag = false;
                            }
                        }
                    }
                }
            }
        };
    }

    public static <T extends BlockEntity & HasStorage> BlockEntityTicker<T> passFluid() {
        return (world, pos, state, blockEntity) -> {
            var storage = blockEntity.getStorage();
            int count = 0;
            for (Direction direction : Direction.values()) {
                var destPos = pos.relative(direction);
                var optional = Optional.ofNullable(world.getBlockEntity(destPos))
                    .flatMap(d -> d.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction.getOpposite()).resolve());
                if (optional.isPresent()) {
                    var handler = optional.get();
                    var fluidMap = new ArrayList<>(storage.getFluidMap().entrySet());
                    for (Map.Entry<FluidKey, Long> entry : fluidMap) {
                        var filled = handler.fill(entry.getKey().toStack((int) Math.min(entry.getValue(), Integer.MAX_VALUE)), IFluidHandler.FluidAction.EXECUTE);
                        if (filled > 0) { // Fluid is transferred.
                            storage.putFluid(entry.getKey(), entry.getValue() - filled);
                            count += 1;
                            if (count > MAX_TRANSFER) return;
                        }
                    }
                }
            }
        };
    }

    private class StorageItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return itemMap.size();
        }

        private Optional<Map.Entry<ItemKey, Long>> getByIndex(int index) {
            if (0 <= index && index < getSlots()) {
                return Optional.of(Iterators.get(itemMap.entrySet().iterator(), index));
            } else {
                return Optional.empty();
            }
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return getByIndex(slot)
                .map(e -> e.getKey().toStack((int) Math.min(e.getValue(), Integer.MAX_VALUE)))
                .orElse(ItemStack.EMPTY);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            // Not insertable
            return stack;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            var entry = getByIndex(slot);
            if (entry.isPresent()) {
                var key = entry.get().getKey();
                long storageCount = entry.get().getValue();
                int size = (int) Math.min(amount, storageCount);
                if (!simulate) {
                    if (storageCount > amount) {
                        itemMap.put(key, storageCount - amount);
                    } else {
                        itemMap.remove(key);
                    }
                }
                return key.toStack(size);
            } else {
                return ItemStack.EMPTY;
            }
        }

        @Override
        public int getSlotLimit(int slot) {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            // Not insertable
            return false;
        }
    }
}
