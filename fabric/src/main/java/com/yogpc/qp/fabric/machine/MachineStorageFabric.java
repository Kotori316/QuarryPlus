package com.yogpc.qp.fabric.machine;

import com.yogpc.qp.machine.MachineStorage;
import com.yogpc.qp.machine.MachineStorageFactory;
import com.yogpc.qp.machine.MachineStorageHolder;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

import java.util.Collections;
import java.util.Iterator;

public final class MachineStorageFabric extends MachineStorage {
    public static class Factory implements MachineStorageFactory {
        @Override
        public MachineStorage createMachineStorage() {
            return new MachineStorageFabric();
        }
    }

    static ItemKey fromItemVariant(ItemVariant variant) {
        return new ItemKey(variant.getItem(), variant.getComponents());
    }

    static FluidKey fromFluidVariant(FluidVariant variant) {
        return new FluidKey(variant.getFluid(), variant.getComponents());
    }

    public static final class ItemStorageImpl<T> extends SnapshotParticipant<Object2LongLinkedOpenHashMap<ItemKey>> implements Storage<ItemVariant> {
        private final MachineStorageHolder<T> holder;
        private final T instance;

        public ItemStorageImpl(MachineStorageHolder<T> holder, T instance) {
            this.holder = holder;
            this.instance = instance;
        }

        private MachineStorageFabric storage() {
            return (MachineStorageFabric) holder.getMachineStorage(instance);
        }

        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            if (resource.isBlank() || maxAmount <= 0) {
                return 0;
            }
            updateSnapshots(transaction);
            storage().items.addTo(fromItemVariant(resource), maxAmount);
            return maxAmount;
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            if (resource.isBlank() || maxAmount <= 0) {
                return 0;
            }
            var key = fromItemVariant(resource);
            var contains = storage().items.getLong(key);
            long toExtract = Math.min(maxAmount, contains);
            if (toExtract <= 0) {
                return 0;
            }
            updateSnapshots(transaction);
            if (contains <= toExtract) {
                storage().items.removeLong(key);
            } else {
                storage().items.put(key, contains - toExtract);
            }

            return toExtract;
        }

        @Override
        public Iterator<StorageView<ItemVariant>> iterator() {
            return Collections.emptyIterator();
        }

        @Override
        protected Object2LongLinkedOpenHashMap<ItemKey> createSnapshot() {
            return storage().items.clone();
        }

        @Override
        protected void readSnapshot(Object2LongLinkedOpenHashMap<ItemKey> snapshot) {
            storage().items.clear();
            storage().items.putAll(snapshot);
        }

        @Override
        protected void onFinalCommit() {
            super.onFinalCommit();
            storage().notifyUpdate();
        }
    }

    public static final class FluidStorageImpl<T> extends SnapshotParticipant<Object2LongLinkedOpenHashMap<FluidKey>> implements Storage<FluidVariant> {
        private final MachineStorageHolder<T> holder;
        private final T instance;

        public FluidStorageImpl(MachineStorageHolder<T> holder, T instance) {
            this.holder = holder;
            this.instance = instance;
        }

        private MachineStorageFabric storage() {
            return (MachineStorageFabric) holder.getMachineStorage(instance);
        }

        @Override
        public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            if (resource.isBlank() || maxAmount <= 0) {
                return 0;
            }
            updateSnapshots(transaction);
            storage().fluids.addTo(fromFluidVariant(resource), maxAmount);
            return maxAmount;
        }

        @Override
        public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            if (resource.isBlank() || maxAmount <= 0) {
                return 0;
            }
            var key = fromFluidVariant(resource);
            var contains = storage().fluids.getLong(key);
            long toExtract = Math.min(maxAmount, contains);
            if (toExtract <= 0) {
                return 0;
            }
            updateSnapshots(transaction);
            if (contains <= toExtract) {
                storage().fluids.removeLong(key);
            } else {
                storage().fluids.put(key, contains - toExtract);
            }

            return toExtract;
        }

        @Override
        public Iterator<StorageView<FluidVariant>> iterator() {
            return Collections.emptyIterator();
        }

        @Override
        protected Object2LongLinkedOpenHashMap<FluidKey> createSnapshot() {
            return storage().fluids.clone();
        }

        @Override
        protected void readSnapshot(Object2LongLinkedOpenHashMap<FluidKey> snapshot) {
            storage().fluids.clear();
            storage().fluids.putAll(snapshot);
        }
    }

    /**
     * Like {@link FluidStorageImpl}, but insertion is always refused; used to expose a machine's tank for
     * external extraction only. Unlike {@link FluidStorageImpl#iterator()}, {@link #iterator()} here actually
     * enumerates the held fluids, so pipes/hoppers that discover extractable content by iterating can find them.
     */
    public static final class ExtractOnlyFluidStorageImpl<T> extends SnapshotParticipant<Object2LongLinkedOpenHashMap<FluidKey>> implements Storage<FluidVariant> {
        private final MachineStorageHolder<T> holder;
        private final T instance;

        public ExtractOnlyFluidStorageImpl(MachineStorageHolder<T> holder, T instance) {
            this.holder = holder;
            this.instance = instance;
        }

        private MachineStorageFabric storage() {
            return (MachineStorageFabric) holder.getMachineStorage(instance);
        }

        @Override
        public boolean supportsInsertion() {
            return false;
        }

        @Override
        public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            return 0;
        }

        @Override
        public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            if (resource.isBlank() || maxAmount <= 0) {
                return 0;
            }
            var key = fromFluidVariant(resource);
            var contains = storage().fluids.getLong(key);
            long toExtract = Math.min(maxAmount, contains);
            if (toExtract <= 0) {
                return 0;
            }
            updateSnapshots(transaction);
            if (contains <= toExtract) {
                storage().fluids.removeLong(key);
            } else {
                storage().fluids.put(key, contains - toExtract);
            }

            return toExtract;
        }

        @Override
        public Iterator<StorageView<FluidVariant>> iterator() {
            return storage().fluidKeyCounts().stream()
                .<StorageView<FluidVariant>>map(this::toView)
                .iterator();
        }

        private StorageView<FluidVariant> toView(FluidKeyCount entry) {
            var variant = FluidVariant.of(entry.key().fluid(), entry.key().patch());
            var amount = entry.count();
            return new StorageView<>() {
                @Override
                public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
                    return ExtractOnlyFluidStorageImpl.this.extract(resource, maxAmount, transaction);
                }

                @Override
                public boolean isResourceBlank() {
                    return false;
                }

                @Override
                public FluidVariant getResource() {
                    return variant;
                }

                @Override
                public long getAmount() {
                    return amount;
                }

                @Override
                public long getCapacity() {
                    return amount;
                }
            };
        }

        @Override
        protected Object2LongLinkedOpenHashMap<FluidKey> createSnapshot() {
            return storage().fluids.clone();
        }

        @Override
        protected void readSnapshot(Object2LongLinkedOpenHashMap<FluidKey> snapshot) {
            storage().fluids.clear();
            storage().fluids.putAll(snapshot);
        }
    }
}
