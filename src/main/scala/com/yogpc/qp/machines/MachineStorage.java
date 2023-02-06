package com.yogpc.qp.machines;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.collect.Iterators;
import com.yogpc.qp.utils.MapMulti;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

public class MachineStorage {
    protected Map<ItemKey, Long> itemMap = new LinkedHashMap<>();
    protected Map<FluidKey, Long> fluidMap = new LinkedHashMap<>();
    protected LazyOptional<IItemHandler> itemHandler;
    protected LazyOptional<IFluidHandler> fluidHandler;

    public MachineStorage() {
        setHandler();
    }

    protected void setHandler() {
        itemHandler = LazyOptional.of(StorageItemHandler::new);
        fluidHandler = LazyOptional.of(StorageFluidHandler::new);
    }

    public void addItem(ItemStack stack) {
        if (stack.isEmpty()) return; // No need to store empty item.
        var key = new ItemKey(stack);
        itemMap.merge(key, (long) stack.getCount(), Long::sum);
    }

    public void addAllItems(Map<ItemKey, Long> drops) {
        drops.forEach((itemKey, count) -> this.itemMap.merge(itemKey, count, Long::sum));
    }

    public void addFluid(ItemStack bucketItem) {
        FluidUtil.getFluidContained(bucketItem).ifPresent(f -> {
            var key = new FluidKey(f);
            fluidMap.merge(key, (long) f.getAmount(), Long::sum);
        });
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
            .collect(Collectors.toMap(ItemKey::fromNbt, n -> n.getLong("count")));
        var fluidTag = tag.getList("fluids", Tag.TAG_COMPOUND);
        fluidMap = fluidTag.stream()
            .mapMulti(MapMulti.cast(CompoundTag.class))
            .collect(Collectors.toMap(FluidKey::fromNbt, n -> n.getLong("amount")));
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

    public interface HasStorage {
        MachineStorage getStorage();
    }

    private static final int MAX_TRANSFER = 4;
    @VisibleForTesting
    static final List<Direction> INSERT_ORDER = List.of(Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST, Direction.DOWN, Direction.UP);

    public static <T extends BlockEntity & HasStorage> BlockEntityTicker<T> passItems() {
        return (world, pos, state, blockEntity) -> {
            var storage = blockEntity.getStorage();
            int count = 0;
            for (var direction : INSERT_ORDER) {
                var destination = Optional.ofNullable(world.getBlockEntity(pos.relative(direction)));
                var handler = destination.flatMap(d -> d.getCapability(ForgeCapabilities.ITEM_HANDLER, direction.getOpposite()).resolve()).orElse(null);
                if (handler == null) continue;
                var itemMap = new ArrayList<>(storage.itemMap.entrySet());
                for (Map.Entry<ItemKey, Long> entry : itemMap) {
                    long beforeCount = entry.getValue();
                    while (beforeCount > 0) {
                        var key = entry.getKey();
                        int itemCount = (int) Math.min(key.toStack(1).getMaxStackSize(), beforeCount);
                        var rest = ItemHandlerHelper.insertItem(handler, key.toStack(itemCount), false);
                        if (itemCount != rest.getCount()) {
                            // Item transferred.
                            int transferred = itemCount - rest.getCount();
                            TraceQuarryWork.transferItem(blockEntity, handler, key, transferred);
                            long remain = beforeCount - transferred;
                            beforeCount = remain;
                            if (remain > 0) {
                                // the item still exists.
                                storage.itemMap.put(key, remain);
                            } else {
                                // the items all have been transferred.
                                storage.itemMap.remove(key);
                            }

                            count += 1;
                            if (count >= MAX_TRANSFER) return;
                        } else {
                            break;
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
            for (Direction direction : INSERT_ORDER) {
                var destPos = pos.relative(direction);
                var handler = Optional.ofNullable(world.getBlockEntity(destPos))
                    .flatMap(d -> d.getCapability(ForgeCapabilities.FLUID_HANDLER, direction.getOpposite()).resolve())
                    .orElse(null);
                if (handler == null) continue;
                var fluidMap = new ArrayList<>(storage.getFluidMap().entrySet());
                for (Map.Entry<FluidKey, Long> entry : fluidMap) {
                    var filled = handler.fill(entry.getKey().toStack((int) Math.min(entry.getValue(), Integer.MAX_VALUE)), IFluidHandler.FluidAction.EXECUTE);
                    if (filled > 0) { // Fluid is transferred.
                        TraceQuarryWork.transferFluid(blockEntity, handler, entry.getKey(), filled);
                        storage.putFluid(entry.getKey(), entry.getValue() - filled);
                        count += 1;
                        if (count > MAX_TRANSFER) return;
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

        @NotNull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return getByIndex(slot)
                .map(e -> e.getKey().toStack((int) Math.min(e.getValue(), Integer.MAX_VALUE)))
                .orElse(ItemStack.EMPTY);
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            // Not insertable
            return stack;
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            var entry = getByIndex(slot).orElse(null);
            if (entry != null) {
                var key = entry.getKey();
                long storageCount = entry.getValue();
                int size = (int) Math.min(amount, storageCount);
                if (!simulate) {
                    TraceQuarryWork.transferItem(null, null, key, size);
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
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            // Not insertable
            return false;
        }
    }

    private final class StorageFluidHandler implements IFluidHandler {
        @Override
        public int getTanks() {
            return fluidMap.size();
        }

        private Optional<Map.Entry<FluidKey, Long>> getByIndex(int index) {
            if (0 <= index && index < getTanks()) {
                return Optional.of(Iterators.get(fluidMap.entrySet().iterator(), index));
            } else {
                return Optional.empty();
            }
        }

        @NotNull
        @Override
        public FluidStack getFluidInTank(int tank) {
            return getByIndex(tank)
                .map(e -> e.getKey().toStack((int) Math.min(e.getValue(), Integer.MAX_VALUE)))
                .orElse(FluidStack.EMPTY);
        }

        @Override
        public int getTankCapacity(int tank) {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            // Not insertable
            return false;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            // Not insertable
            return 0;
        }

        @NotNull
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            var key = new FluidKey(resource);
            return Optional.ofNullable(fluidMap.get(key))
                .map(l -> Map.entry(key, l))
                .map(e -> drainInternal(e, resource.getAmount(), action))
                .orElse(FluidStack.EMPTY);
        }

        @NotNull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            var iterator = fluidMap.entrySet().iterator();
            if (iterator.hasNext()) {
                return drainInternal(iterator.next(), maxDrain, action);
            } else {
                return FluidStack.EMPTY;
            }
        }

        private FluidStack drainInternal(Map.Entry<FluidKey, Long> entry, int maxDrain, FluidAction action) {
            var drained = entry.getKey().toStack((int) Math.min(entry.getValue(), maxDrain));
            if (action.execute()) {
                TraceQuarryWork.transferFluid(null, null, entry.getKey(), drained.getAmount());
                putFluid(entry.getKey(), entry.getValue() - drained.getAmount());
            }
            return drained;
        }
    }
}
