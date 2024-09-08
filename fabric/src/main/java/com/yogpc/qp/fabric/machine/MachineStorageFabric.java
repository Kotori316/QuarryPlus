package com.yogpc.qp.fabric.machine;

import com.yogpc.qp.machine.MachineStorage;
import com.yogpc.qp.machine.MachineStorageFactory;
import com.yogpc.qp.machine.MachineStorageHolder;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
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

    public static class ItemStorageImpl<T> extends SnapshotParticipant<Object2LongLinkedOpenHashMap<ItemKey>> implements Storage<ItemVariant> {
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
}
