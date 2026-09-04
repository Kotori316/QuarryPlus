package com.yogpc.qp.machine;

import com.google.common.collect.Iterators;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yogpc.qp.FluidStackLike;
import com.yogpc.qp.FluidStackLikeUnit;
import com.yogpc.qp.PlatformAccess;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.*;
import java.util.stream.Collectors;

public class MachineStorage {
    public static final int ONE_BUCKET = 81000;

    protected final Object2LongLinkedOpenHashMap<ItemKey> items = new Object2LongLinkedOpenHashMap<>();
    /**
     * Unit is Fabric one
     */
    protected final Object2LongLinkedOpenHashMap<FluidKey> fluids = new Object2LongLinkedOpenHashMap<>();
    List<Runnable> onUpdate = new ArrayList<>();

    /**
     * The implementation is provided via a static service file, so it is resolved once and reused.
     * This method is called for every machine and for every entry decoded from NBT, so the
     * {@link ServiceLoader} scan it used to perform was far from free.
     */
    private static final class FactoryHolder {
        private FactoryHolder() {
        }

        static final MachineStorageFactory FACTORY =
            ServiceLoader.load(MachineStorageFactory.class, MachineStorageFactory.class.getClassLoader())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not find Machine Storage implementation"));
    }

    public static MachineStorage of() {
        return FactoryHolder.FACTORY.createMachineStorage();
    }

    static MachineStorage of(Map<ItemKey, Long> items, Map<FluidKey, Long> fluids) {
        var storage = of();
        storage.items.putAll(items);
        storage.fluids.putAll(fluids);
        return storage;
    }

    protected MachineStorage() {
        items.defaultReturnValue(0L);
        fluids.defaultReturnValue(0L);
    }

    public void onUpdate(Runnable runnable) {
        onUpdate.add(runnable);
    }

    protected void notifyUpdate() {
        onUpdate.forEach(Runnable::run);
    }

    public void addItem(ItemStack stack) {
        if (stack.isEmpty()) return;
        addItem(ItemStackTemplate.fromNonEmptyStack(stack));
    }

    @VisibleForTesting
    public void addItem(ItemStackTemplate template) {
        var key = ItemKey.of(template);
        items.addTo(key, template.count());
        notifyUpdate();
    }

    public void addFluid(Fluid fluid, long amount) {
        if (fluid.isSame(Fluids.EMPTY)) return;
        var key = new FluidKey(fluid, DataComponentPatch.EMPTY);
        fluids.addTo(key, amount);
        notifyUpdate();
    }

    public void addBucketFluid(ItemStack stack) {
        if (stack.isEmpty()) return;
        var content = PlatformAccess.getAccess().getFluidInItem(stack);
        if (content.fluid().isSame(Fluids.EMPTY)) return;
        var key = new FluidKey(content.fluid(), content.patch());
        fluids.addTo(key, content.amount().commonAmount());
        notifyUpdate();
    }

    long getItemCount(ItemKey key) {
        return items.getLong(key);
    }

    public long getItemCount(Item item, DataComponentPatch patch) {
        return getItemCount(new ItemKey(item, patch));
    }

    long getFluidCount(FluidKey key) {
        return fluids.getLong(key);
    }

    public long getFluidCount(Fluid fluid) {
        return getFluidCount(new FluidKey(fluid, DataComponentPatch.EMPTY));
    }

    @Override
    public String toString() {
        var itemSize = items.values().longStream().filter(t -> t != 0L).count();
        var fluidSize = fluids.values().longStream().filter(t -> t != 0L).count();
        return "MachineStorage{" +
            "items=" + itemSize +
            ", fluids=" + fluidSize +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MachineStorage that = (MachineStorage) o;
        return Objects.equals(items, that.items) && Objects.equals(fluids, that.fluids);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items, fluids);
    }

    public record ItemKey(Item item, DataComponentPatch patch) {
        public static ItemKey of(ItemStack stack) {
            return new ItemKey(stack.getItem(), stack.getComponentsPatch());
        }

        @VisibleForTesting
        public static ItemKey of(ItemStackTemplate stack) {
            return new ItemKey(stack.item().value(), stack.components());
        }

        public ItemStack toStack(int count) {
            var stack = new ItemStack(item, count);
            stack.applyComponents(patch);
            return stack;
        }

        @VisibleForTesting
        public ItemStackTemplate toTemplate(int count) {
            var template = new ItemStackTemplate(item, patch);
            return template.withCount(count);
        }
    }

    public record FluidKey(Fluid fluid, DataComponentPatch patch) {
        public FluidStackLike toStack(long amount) {
            return new FluidStackLike(fluid, FluidStackLikeUnit.fromCommon(amount), patch);
        }
    }

    public record ItemKeyCount(ItemKey key, long count) {
        static Map<ItemKey, Long> list2Map(List<ItemKeyCount> list) {
            return list.stream().collect(Collectors.toMap(ItemKeyCount::key, ItemKeyCount::count));
        }
    }

    /**
     * @param count Unit is fabric one, 81000 equals to 1 bucket.
     */
    public record FluidKeyCount(FluidKey key, long count) {
        static Map<FluidKey, Long> list2Map(List<FluidKeyCount> list) {
            return list.stream().collect(Collectors.toMap(FluidKeyCount::key, FluidKeyCount::count));
        }
    }

    public List<ItemKeyCount> itemKeyCounts() {
        return items.object2LongEntrySet().stream()
            .map(e -> new ItemKeyCount(e.getKey(), e.getLongValue()))
            .toList();
    }

    public List<FluidKeyCount> fluidKeyCounts() {
        return fluids.object2LongEntrySet().stream()
            .map(e -> new FluidKeyCount(e.getKey(), e.getLongValue()))
            .toList();
    }

    public static final MapCodec<ItemKey> ITEM_KEY_MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        RecordCodecBuilder.of(ItemKey::item, "item", BuiltInRegistries.ITEM.byNameCodec()),
        DataComponentPatch.CODEC.optionalFieldOf("patch", DataComponentPatch.EMPTY).forGetter(ItemKey::patch)
    ).apply(i, ItemKey::new));

    static final MapCodec<ItemKeyCount> ITEM_KEY_COUNT_MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        RecordCodecBuilder.of(ItemKeyCount::key, ITEM_KEY_MAP_CODEC),
        RecordCodecBuilder.of(ItemKeyCount::count, "count", Codec.LONG)
    ).apply(i, ItemKeyCount::new));

    static final MapCodec<FluidKeyCount> FLUID_KEY_COUNT_MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        RecordCodecBuilder.of(c -> c.key.fluid, "fluid", BuiltInRegistries.FLUID.byNameCodec()),
        DataComponentPatch.CODEC.optionalFieldOf("patch", DataComponentPatch.EMPTY).forGetter(c -> c.key.patch),
        RecordCodecBuilder.of(FluidKeyCount::count, "count", Codec.LONG)
    ).apply(i, (a, b, c) -> new FluidKeyCount(new FluidKey(a, b), c)));

    public static final MapCodec<MachineStorage> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        RecordCodecBuilder.of(MachineStorage::itemKeyCounts, "items", ITEM_KEY_COUNT_MAP_CODEC.codec().listOf()),
        RecordCodecBuilder.of(MachineStorage::fluidKeyCounts, "fluids", FLUID_KEY_COUNT_MAP_CODEC.codec().listOf())
    ).apply(i, (itemKeyCounts, fluidKeyCounts) -> MachineStorage.of(
        ItemKeyCount.list2Map(itemKeyCounts),
        FluidKeyCount.list2Map(fluidKeyCounts)
    )));

    private static final int MAX_TRANSFER = 4;

    public void passItems(Level level, BlockPos storagePos) {
        var mutablePos = new BlockPos.MutableBlockPos();
        int count = 0;
        Direction[] directions = {Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST, Direction.DOWN, Direction.UP};
        root:
        for (Direction direction : directions) {
            var pos = mutablePos.setWithOffset(storagePos, direction);
            var state = level.getBlockState(pos);
            if (cantBeStorage(state)) {
                continue;
            }
            var itr = items.object2LongEntrySet().fastIterator();
            while (itr.hasNext()) {
                var entry = itr.next();
                var stack = entry.getKey().toStack(Math.clamp(entry.getLongValue(), 0, Integer.MAX_VALUE));
                var rest = PlatformAccess.getAccess().transfer().transferItem(level, pos, stack, direction.getOpposite(), false);
                if (rest.getCount() == stack.getCount()) {
                    continue;
                }
                if (rest.isEmpty() && stack.getCount() == entry.getLongValue()) {
                    itr.remove();
                } else {
                    entry.setValue((entry.getLongValue() - stack.getCount()) + rest.getCount());
                }
                if (count++ > MAX_TRANSFER) {
                    break root;
                }
            }
        }
        if (count > 0) {
            notifyUpdate();
        }
    }

    public void passFluids(Level level, BlockPos storagePos) {
        var mutablePos = new BlockPos.MutableBlockPos();
        int count = 0;
        Direction[] directions = {Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST, Direction.DOWN, Direction.UP};
        root:
        for (Direction direction : directions) {
            var pos = mutablePos.setWithOffset(storagePos, direction);
            var state = level.getBlockState(pos);
            if (cantBeStorage(state)) {
                continue;
            }
            var itr = fluids.object2LongEntrySet().fastIterator();
            while (itr.hasNext()) {
                var entry = itr.next();
                var stack = entry.getKey().toStack(entry.getLongValue());
                var rest = PlatformAccess.getAccess().transfer().transferFluid(level, pos, stack, direction.getOpposite(), false);
                if (rest.amount() == stack.amount()) {
                    continue;
                }
                if (rest.isEmpty() && stack.amount().commonAmount() == entry.getLongValue()) {
                    itr.remove();
                } else {
                    entry.setValue((entry.getLongValue() - stack.amount().commonAmount()) + rest.amount().commonAmount());
                }
                if (count++ > MAX_TRANSFER) {
                    break root;
                }
            }
        }
        if (count > 0) {
            notifyUpdate();
        }
    }

    private static boolean cantBeStorage(BlockState state) {
        return state.isAir()
            || state.is(PlatformAccess.getAccess().registerObjects().frameBlock().get())
            || state.is(PlatformAccess.getAccess().registerObjects().generatorBlock().get());
    }

    // For Forge FluidHandler
    public int fluidTanks() {
        return fluids.size();
    }

    public FluidStackLike getFluidByIndex(int i) {
        if (i < 0 || i >= fluids.size()) {
            return FluidStackLike.EMPTY;
        }
        var e = Iterators.get(fluids.object2LongEntrySet().iterator(), i);
        return new FluidStackLike(e.getKey().fluid(), FluidStackLikeUnit.fromCommon(e.getLongValue()), e.getKey().patch());
    }

    public FluidStackLike drainFluid(FluidStackLike toDrain, boolean execute) {
        var key = new FluidKey(toDrain.fluid(), toDrain.patch());
        var amount = fluids.getLong(key);
        var toDrainAmount = Math.min(amount, toDrain.amount().commonAmount());
        if (execute) {
            if (amount - toDrainAmount > 0) {
                fluids.put(key, amount - toDrainAmount);
            } else {
                fluids.removeLong(key);
            }
            notifyUpdate();
        }
        return toDrain.withAmount(FluidStackLikeUnit.fromCommon(toDrainAmount));
    }

    public FluidStackLike drainFluidByIndex(int index, long amount, boolean execute) {
        var fluid = getFluidByIndex(index);
        if (fluid.isEmpty()) {
            return FluidStackLike.EMPTY;
        }
        return drainFluid(fluid.withAmount(FluidStackLikeUnit.fromCommon(amount)), execute);
    }

    // For Forge ItemHandler
    public int itemSlots() {
        return items.size();
    }

    public ItemStack getItemByIndex(int i) {
        if (i < 0 || i >= items.size()) {
            return ItemStack.EMPTY;
        }
        var e = Iterators.get(items.object2LongEntrySet().iterator(), i);
        return e.getKey().toStack(Math.clamp(e.getLongValue(), 0, Integer.MAX_VALUE));
    }

    public ItemStack extractItemByIndex(int i, int amount, boolean execute) {
        if (i < 0 || i >= items.size()) {
            return ItemStack.EMPTY;
        }
        var e = Iterators.get(items.object2LongEntrySet().iterator(), i);
        var toExtractAmount = Math.min(amount, e.getLongValue());
        if (execute) {
            if (amount - toExtractAmount > 0) {
                items.put(e.getKey(), amount - toExtractAmount);
            } else {
                items.removeLong(e.getKey());
            }
            notifyUpdate();
        }
        return e.getKey().toStack(Math.clamp(toExtractAmount, 0, Integer.MAX_VALUE));
    }

    public static <T extends BlockEntity> BlockEntityTicker<T> pushItemTicker() {
        return (level, blockPos, blockState, blockEntity) -> MachineStorageHolder.getHolder(blockEntity).ifPresent(h -> h.getMachineStorage(blockEntity).passItems(level, blockPos));
    }

    public static <T extends BlockEntity> BlockEntityTicker<T> pushFluidTicker() {
        return (level, blockPos, blockState, blockEntity) -> MachineStorageHolder.getHolder(blockEntity).ifPresent(h -> h.getMachineStorage(blockEntity).passFluids(level, blockPos));
    }
}
