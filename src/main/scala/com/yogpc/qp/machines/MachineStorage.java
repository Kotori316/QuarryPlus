package com.yogpc.qp.machines;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.yogpc.qp.integration.QuarryFluidTransfer;
import com.yogpc.qp.utils.MapMulti;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.Direction;
import org.apache.commons.lang3.tuple.Pair;

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

    public void addFluid(Fluid fluid, long amount) {
        var key = new FluidKey(fluid, null);
        fluidMap.merge(key, amount, (l1, l2) -> {
                long a = l1 + l2;
                if (a > 0) return a;
                else return null;
            }
        );
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
            .mapMulti(MapMulti.cast(NbtCompound.class))
            .map(n -> Pair.of(ItemKey.fromNbt(n), n.getLong("count")))
            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
        var fluidTag = tag.getList("fluids", NbtElement.COMPOUND_TYPE);
        fluidMap = fluidTag.stream()
            .mapMulti(MapMulti.cast(NbtCompound.class))
            .map(n -> Pair.of(FluidKey.fromNbt(n), n.getLong("amount")))
            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    public Map<FluidKey, Long> getFluidMap() {
        return Map.copyOf(fluidMap); // Return copy to avoid ConcurrentModificationException
    }

    private void putFluid(Fluid fluid, long amount) {
        var key = new FluidKey(fluid, null);
        if (amount <= 0) {
            fluidMap.remove(key);
        } else {
            fluidMap.put(key, amount);
        }
    }

    private static final Map<BucketItem, Fluid> BUCKET_ITEM_FLUID_MAP = new HashMap<>();

    private static FluidKey getFluidInBucket(BucketItem bucket) {
        // How do I get nbt of Fluid?
        var fluid = BUCKET_ITEM_FLUID_MAP.computeIfAbsent(bucket, t -> {
            try {
                return (Fluid) BUCKET_FLUID_FIELD.get(t);
            } catch (ReflectiveOperationException ignore) {
                return Fluids.EMPTY;
            }
        });
        return new FluidKey(fluid, null);
    }

    private static final Field BUCKET_FLUID_FIELD;
    public static final long ONE_BUCKET = 1000;

    static {
        try {
            var bucketItemClassName = FabricLoader.getInstance().getMappingResolver().unmapClassName("intermediary", BucketItem.class.getName());
            var fluidClassName = FabricLoader.getInstance().getMappingResolver().unmapClassName("intermediary", Fluid.class.getName());

            var fluidFieldBucketItem = FabricLoader.getInstance().getMappingResolver().mapFieldName("intermediary",
                bucketItemClassName, "field_7905", "L%s;".formatted(fluidClassName.replace('.', '/')));
            BUCKET_FLUID_FIELD = BucketItem.class.getDeclaredField(fluidFieldBucketItem);
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

    public static <T extends BlockEntity & HasStorage> BlockEntityTicker<T> passFluid() {
        if (QuarryFluidTransfer.isRegistered()) return (world, pos, state, blockEntity) -> {
            var storage = blockEntity.getStorage();
            int count = 0;
            for (Direction direction : Direction.values()) {
                var destPos = pos.offset(direction);
                var tile = world.getBlockEntity(destPos);
                if (tile != null) {
                    var fluidMap = new ArrayList<>(storage.getFluidMap().entrySet());
                    for (Map.Entry<FluidKey, Long> entry : fluidMap) {
                        var excess = QuarryFluidTransfer.transfer(world, destPos, tile, entry.getKey().fluid(), entry.getValue(), direction.getOpposite());
                        if (!entry.getValue().equals(excess.getValue())) { // Fluid is transferred.
                            storage.putFluid(entry.getKey().fluid(), excess.getValue());
                            count += 1;
                            if (count > MAX_TRANSFER) return;
                        }
                    }
                }
            }
        };
        else return null;
    }
}
