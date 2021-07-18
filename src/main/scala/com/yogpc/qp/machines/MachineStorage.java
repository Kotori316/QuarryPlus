package com.yogpc.qp.machines;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

public class MachineStorage {
    private Map<ItemKey, Long> itemMap = new LinkedHashMap<>();
    private Map<FluidKey, Long> fluidMap = new LinkedHashMap<>();

    public void addItem(ItemStack stack) {
        if (stack.isEmpty()) return; // No need to store empty item.
        var key = new ItemKey(stack);
        itemMap.merge(key, (long) stack.getCount(), Long::sum);
    }

    public void addFluid(ItemStack bucketItem) {
        if (bucketItem.getItem() instanceof BucketItem bucket) {
            var key = getFluidInBucket(bucket);
            fluidMap.merge(key, ONE_BUCKET, Long::sum);
        }
    }

    public NbtCompound toNbt() {
        var tag = new NbtCompound();
        var itemTag = new NbtList();
        itemMap.forEach((itemKey, count) -> itemTag.add(itemKey.createNbt(count)));
        var fluidTag = new NbtList();
        fluidMap.forEach((fluidKey, amount) -> fluidTag.add(fluidKey.createNbt(amount)));
        tag.put("items", itemTag);
        tag.put("fluids", fluidTag);
        return tag;
    }

    public void readNbt(NbtCompound tag) {
        var itemTag = tag.getList("items", NbtElement.COMPOUND_TYPE);
        itemMap = itemTag.stream()
            .map(NbtCompound.class::cast)
            .map(n -> Pair.of(ItemKey.fromNbt(n), n.getLong("count")))
            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
        var fluidTag = tag.getList("fluids", NbtElement.COMPOUND_TYPE);
        fluidMap = fluidTag.stream()
            .map(NbtCompound.class::cast)
            .map(n -> Pair.of(FluidKey.fromNbt(n), n.getLong("amount")))
            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    public record ItemKey(Item item, @Nullable NbtCompound nbt) {
        public ItemKey(ItemStack stack) {
            this(stack.getItem(), stack.getNbt());
        }

        public NbtCompound createNbt(long itemCount) {
            var tag = new NbtCompound();
            tag.putString("item", Registry.ITEM.getId(item).toString());
            if (nbt != null)
                tag.put("tag", nbt);
            tag.putLong("count", itemCount);
            return tag;
        }

        static ItemKey fromNbt(NbtCompound tag) {
            var item = Registry.ITEM.get(new Identifier(tag.getString("item")));
            var nbt = tag.contains("tag") ? tag.getCompound("tag") : null;
            return new ItemKey(item, nbt);
        }

        public ItemStack toStack(int count) {
            var stack = new ItemStack(item, count);
            stack.setNbt(nbt);
            return stack;
        }
    }

    public record FluidKey(Fluid fluid, @Nullable NbtCompound nbt) {

        public NbtCompound createNbt(long amount) {
            var tag = new NbtCompound();
            tag.putString("fluid", Registry.FLUID.getId(fluid).toString());
            if (nbt != null)
                tag.put("tag", nbt);
            tag.putLong("amount", amount);
            return tag;
        }

        static FluidKey fromNbt(NbtCompound tag) {
            var fluid = Registry.FLUID.get(new Identifier(tag.getString("fluid")));
            var nbt = tag.contains("nbt") ? tag.getCompound("nbt") : null;
            return new FluidKey(fluid, nbt);
        }
    }

    private static FluidKey getFluidInBucket(BucketItem bucket) {
        try {
            // How do I get nbt of Fluid?
            return new FluidKey((Fluid) BUCKET_FLUID_FIELD.get(bucket), null);
        } catch (ReflectiveOperationException ignore) {
            return new FluidKey(Fluids.EMPTY, null);
        }
    }

    private static final Field BUCKET_FLUID_FIELD;
    private static final long ONE_BUCKET = 1000;

    static {
        try {
            BUCKET_FLUID_FIELD = BucketItem.class.getDeclaredField("fluid");
            BUCKET_FLUID_FIELD.trySetAccessible();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
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
                var inventory = HopperBlockEntity.getInventoryAt(world, pos.offset(direction));
                if (inventory != null) {
                    var itemMap = new ArrayList<>(storage.itemMap.entrySet());
                    for (Map.Entry<ItemKey, Long> entry : itemMap) {
                        long beforeCount = entry.getValue();
                        boolean flag = true;
                        while (beforeCount > 0 && flag) {
                            int itemCount = (int) Math.min(entry.getKey().item().getMaxCount(), beforeCount);
                            var rest = HopperBlockEntity.transfer(null, inventory, entry.getKey().toStack(itemCount), direction.getOpposite());
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
                                if (count > MAX_TRANSFER) return;
                            } else {
                                flag = false;
                            }
                        }
                    }
                }
            }
        };
    }
}
