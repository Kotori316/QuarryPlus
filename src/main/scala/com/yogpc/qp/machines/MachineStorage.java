package com.yogpc.qp.machines;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.annotations.VisibleForTesting;
import com.yogpc.qp.integration.QuarryFluidTransfer;
import com.yogpc.qp.integration.QuarryItemTransfer;
import com.yogpc.qp.utils.MapMulti;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ExtractionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.apache.commons.lang3.tuple.Pair;

public class MachineStorage {
    protected LinkedHashMap<ItemKey, Long> itemMap = new LinkedHashMap<>();
    protected LinkedHashMap<FluidKey, Long> fluidMap = new LinkedHashMap<>();

    public void addItem(ItemStack stack) {
        if (stack.isEmpty()) return; // No need to store empty item.
        var key = new ItemKey(stack);
        itemMap.merge(key, (long) stack.getCount(), Long::sum);
    }

    public void addFluid(ItemStack bucketItem) {
        if (bucketItem.getItem() instanceof BucketItem bucket) {
            var key = BucketFluidAccessor.getFluidInBucket(bucket);
            fluidMap.merge(key, ONE_BUCKET, Long::sum);
        }
    }

    /**
     * @param fluid  the fluid
     * @param amount the amount in mB, where 1 bucket = 1000 mB
     */
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
            .filter(p -> p.getLeft().item() != Items.AIR)
            .collect(Collectors.toMap(Pair::getKey, Pair::getValue, Long::sum, LinkedHashMap::new));
        var fluidTag = tag.getList("fluids", Tag.TAG_COMPOUND);
        fluidMap = fluidTag.stream()
            .mapMulti(MapMulti.cast(CompoundTag.class))
            .map(n -> Pair.of(FluidKey.fromNbt(n), n.getLong("amount")))
            .filter(p -> p.getLeft().fluid() != Fluids.EMPTY)
            .collect(Collectors.toMap(Pair::getKey, Pair::getValue, Long::sum, LinkedHashMap::new));
    }

    public Map<FluidKey, Long> getFluidMap() {
        return Map.copyOf(fluidMap); // Return copy to avoid ConcurrentModificationException
    }

    /**
     * @param fluid  the fluid
     * @param amount the amount in mB, where 1 bucket = 1000 mB
     */
    private void putFluid(Fluid fluid, long amount) {
        var key = new FluidKey(fluid, null);
        if (amount <= 0) {
            fluidMap.remove(key);
        } else {
            fluidMap.put(key, amount);
        }
    }

    private static class BucketFluidAccessor {
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
    }

    public static final long ONE_BUCKET = 1000;

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
                if (QuarryItemTransfer.destinationExists(world, pos.relative(direction), direction.getOpposite())) {
                    var itemMap = new ArrayList<>(storage.itemMap.entrySet());
                    for (Map.Entry<ItemKey, Long> entry : itemMap) {
                        long beforeCount = entry.getValue();
                        boolean flag = true;
                        while (beforeCount > 0 && flag && count < MAX_TRANSFER) {
                            int itemCount = (int) Math.min(entry.getKey().item().getMaxStackSize(), beforeCount);
                            var rest = QuarryItemTransfer.transfer(world, pos.relative(direction), entry.getKey().toStack(itemCount), direction.getOpposite());
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
            for (Direction direction : INSERT_ORDER) {
                var destPos = pos.relative(direction);
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

    @SuppressWarnings({"UnstableApiUsage", "unused"})
    public static ExtractionOnlyStorage<ItemVariant> getItemStorage(BlockEntity blockEntity, Direction context) {
        if (blockEntity instanceof HasStorage hasStorage) {
            return hasStorage.getStorage().fabricItemStorageSnapshot;
        } else {
            return null;
        }
    }

    @SuppressWarnings({"UnstableApiUsage", "unused"})
    public static ExtractionOnlyStorage<FluidVariant> getFluidStorage(BlockEntity blockEntity, Direction context) {
        if (blockEntity instanceof HasStorage hasStorage) {
            return hasStorage.getStorage().fabricFluidStorageSnapshot;
        } else {
            return null;
        }
    }

    private final FabricItemStorageSnapshot fabricItemStorageSnapshot = new FabricItemStorageSnapshot();
    private final FabricFluidStorageSnapshot fabricFluidStorageSnapshot = new FabricFluidStorageSnapshot();

    @SuppressWarnings("UnstableApiUsage")
    private class FabricItemStorageSnapshot extends SnapshotParticipant<LinkedHashMap<ItemKey, Long>> implements ExtractionOnlyStorage<ItemVariant> {

        @Override
        protected LinkedHashMap<ItemKey, Long> createSnapshot() {
            return new LinkedHashMap<>(itemMap); // Create copy of map, as the field is mutable.
        }

        @Override
        protected void readSnapshot(LinkedHashMap<ItemKey, Long> snapshot) {
            itemMap = snapshot;
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            var key = new ItemKey(resource.getItem(), resource.getNbt());
            if (itemMap.containsKey(key)) {
                long amount = itemMap.get(key);
                var extracted = Math.min(maxAmount, amount);
                var remain = amount - extracted;
                updateSnapshots(transaction);
                if (remain > 0) {
                    // the item still exists.
                    itemMap.put(key, remain);
                } else {
                    // the items all have been transferred.
                    itemMap.remove(key);
                }
                return extracted;
            } else {
                return 0;
            }
        }

        @Override
        public Iterator<StorageView<ItemVariant>> iterator() {
            var combinedStorage = new CombinedStorage<>(itemMap.keySet().stream().map(Element::new).toList());
            return combinedStorage.iterator();
        }

        private final class Element implements ExtractionOnlyStorage<ItemVariant>, SingleSlotStorage<ItemVariant> {
            private final ItemKey itemKey;

            private Element(ItemKey itemKey) {
                this.itemKey = itemKey;
            }

            @Override
            public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
                return FabricItemStorageSnapshot.this.extract(resource, maxAmount, transaction);
            }

            @Override
            public boolean isResourceBlank() {
                return getResource().isBlank();
            }

            @Override
            public ItemVariant getResource() {
                return ItemVariant.of(itemKey.item(), itemKey.nbt());
            }

            @Override
            public long getAmount() {
                return itemMap.getOrDefault(itemKey, 0L);
            }

            @Override
            public long getCapacity() {
                return Integer.MAX_VALUE;
            }

            @Override
            public String toString() {
                return "ItemElement[" +
                       "itemKey=" + itemKey + ", " +
                       "amount=" + getAmount() + ']';
            }
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private class FabricFluidStorageSnapshot extends SnapshotParticipant<LinkedHashMap<FluidKey, Long>> implements ExtractionOnlyStorage<FluidVariant> {

        @Override
        protected LinkedHashMap<FluidKey, Long> createSnapshot() {
            return new LinkedHashMap<>(fluidMap); // Create copy of map, as the field is mutable.
        }

        @Override
        protected void readSnapshot(LinkedHashMap<FluidKey, Long> snapshot) {
            fluidMap = snapshot;
        }

        @Override
        public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            var key = new FluidKey(resource.getFluid(), resource.getNbt());
            if (fluidMap.containsKey(key)) {
                long amount = fluidMap.get(key);
                long amountFabric = amount * FluidConstants.BUCKET / ONE_BUCKET;
                updateSnapshots(transaction);
                long extractedFabric = Math.min(maxAmount, amountFabric);
                long extracted = extractedFabric * ONE_BUCKET / FluidConstants.BUCKET;
                addFluid(key.fluid(), -extracted);
                return extractedFabric;
            } else {
                return 0;
            }
        }

        @Override
        public Iterator<StorageView<FluidVariant>> iterator() {
            var combinedStorage = new CombinedStorage<>(fluidMap.keySet().stream().map(Element::new).toList());
            return combinedStorage.iterator();
        }

        private final class Element implements ExtractionOnlyStorage<FluidVariant>, SingleSlotStorage<FluidVariant> {
            private final FluidKey fluidKey;

            private Element(FluidKey fluidKey) {
                this.fluidKey = fluidKey;
            }

            @Override
            public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
                return FabricFluidStorageSnapshot.this.extract(resource, maxAmount, transaction);
            }

            @Override
            public boolean isResourceBlank() {
                return getResource().isBlank();
            }

            @Override
            public FluidVariant getResource() {
                return FluidVariant.of(fluidKey.fluid(), fluidKey.nbt());
            }

            @Override
            public long getAmount() {
                return fluidMap.getOrDefault(fluidKey, 0L) * FluidConstants.BUCKET / ONE_BUCKET;
            }

            @Override
            public long getCapacity() {
                return Long.MAX_VALUE;
            }

            @Override
            public String toString() {
                return "FluidElement{" +
                       "fluidKey=" + fluidKey + ", " +
                       "amount=" + getAmount() + ']';
            }
        }
    }
}
