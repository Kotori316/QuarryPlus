package com.yogpc.qp.machine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yogpc.qp.PlatformAccess;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public final class MachineStorage {
    public static final int ONE_BUCKET = 81000;

    private final Object2LongLinkedOpenHashMap<ItemKey> items = new Object2LongLinkedOpenHashMap<>();
    /**
     * Unit is Fabric one
     */
    private final Object2LongLinkedOpenHashMap<FluidKey> fluids = new Object2LongLinkedOpenHashMap<>();

    public MachineStorage() {
        items.defaultReturnValue(0L);
        fluids.defaultReturnValue(0L);
    }

    MachineStorage(Map<ItemKey, Long> items, Map<FluidKey, Long> fluids) {
        this();
        this.items.putAll(items);
        this.fluids.putAll(fluids);
    }

    public void addItem(ItemStack stack) {
        if (stack.isEmpty()) return;
        var key = ItemKey.of(stack);
        items.addTo(key, stack.getCount());
    }

    public void addFluid(Fluid fluid, int amount) {
        if (fluid.isSame(Fluids.EMPTY)) return;
        var key = new FluidKey(fluid, DataComponentPatch.EMPTY);
        fluids.addTo(key, amount);
    }

    public void addBucketFluid(ItemStack stack) {
        if (stack.isEmpty()) return;
        var content = PlatformAccess.getAccess().getFluidInItem(stack);
        if (content.fluid().isSame(Fluids.EMPTY)) return;
        var key = new FluidKey(content.fluid(), content.patch());
        fluids.addTo(key, content.amount());
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

    record ItemKey(Item item, DataComponentPatch patch) {
        static ItemKey of(ItemStack stack) {
            return new ItemKey(stack.getItem(), stack.getComponentsPatch());
        }

        ItemStack toStack(int count) {
            return new ItemStack(Holder.direct(item), count, patch);
        }
    }

    record FluidKey(Fluid fluid, DataComponentPatch patch) {
    }

    record ItemKeyCount(ItemKey key, long count) {
        static Map<ItemKey, Long> list2Map(List<ItemKeyCount> list) {
            return list.stream().collect(Collectors.toMap(ItemKeyCount::key, ItemKeyCount::count));
        }
    }

    /**
     * @param count Unit is fabric one, 81000 equals to 1 bucket.
     */
    record FluidKeyCount(FluidKey key, long count) {
        static Map<FluidKey, Long> list2Map(List<FluidKeyCount> list) {
            return list.stream().collect(Collectors.toMap(FluidKeyCount::key, FluidKeyCount::count));
        }
    }

    List<ItemKeyCount> itemKeyCounts() {
        return items.object2LongEntrySet().stream()
            .map(e -> new ItemKeyCount(e.getKey(), e.getLongValue()))
            .toList();
    }

    List<FluidKeyCount> fluidKeyCounts() {
        return fluids.object2LongEntrySet().stream()
            .map(e -> new FluidKeyCount(e.getKey(), e.getLongValue()))
            .toList();
    }

    static final MapCodec<ItemKeyCount> ITEM_KEY_COUNT_MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        RecordCodecBuilder.of(c -> c.key.item, "item", BuiltInRegistries.ITEM.byNameCodec()),
        DataComponentPatch.CODEC.optionalFieldOf("patch").forGetter(c -> Optional.of(c.key.patch)),
        RecordCodecBuilder.of(ItemKeyCount::count, "count", Codec.LONG)
    ).apply(i, (a, b, c) -> new ItemKeyCount(new ItemKey(a, b.orElse(DataComponentPatch.EMPTY)), c)));

    static final MapCodec<FluidKeyCount> FLUID_KEY_COUNT_MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        RecordCodecBuilder.of(c -> c.key.fluid, "fluid", BuiltInRegistries.FLUID.byNameCodec()),
        DataComponentPatch.CODEC.optionalFieldOf("patch").forGetter(c -> Optional.of(c.key.patch)),
        RecordCodecBuilder.of(FluidKeyCount::count, "count", Codec.LONG)
    ).apply(i, (a, b, c) -> new FluidKeyCount(new FluidKey(a, b.orElse(DataComponentPatch.EMPTY)), c)));

    public static final MapCodec<MachineStorage> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        RecordCodecBuilder.of(MachineStorage::itemKeyCounts, "items", ITEM_KEY_COUNT_MAP_CODEC.codec().listOf()),
        RecordCodecBuilder.of(MachineStorage::fluidKeyCounts, "fluids", FLUID_KEY_COUNT_MAP_CODEC.codec().listOf())
    ).apply(i, (itemKeyCounts, fluidKeyCounts) -> new MachineStorage(
        ItemKeyCount.list2Map(itemKeyCounts),
        FluidKeyCount.list2Map(fluidKeyCounts)
    )));

    private static final int MAX_TRANSFER = 4;

    public void passItems(Level level, BlockPos storagePos) {
        var mutablePos = new BlockPos.MutableBlockPos();
        int count = 0;
        Direction[] directions = {Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST, Direction.DOWN, Direction.UP};
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
                    return;
                }
            }
        }
    }

    private static boolean cantBeStorage(BlockState state) {
        return state.isAir()
            || state.is(PlatformAccess.getAccess().registerObjects().frameBlock().get())
            || state.is(PlatformAccess.getAccess().registerObjects().generatorBlock().get());
    }
}
