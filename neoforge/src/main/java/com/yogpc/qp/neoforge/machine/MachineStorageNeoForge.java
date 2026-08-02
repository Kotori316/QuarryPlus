package com.yogpc.qp.neoforge.machine;

import com.yogpc.qp.machine.MachineStorage;
import com.yogpc.qp.machine.MachineStorageFactory;
import com.yogpc.qp.machine.MachineStorageHolder;
import com.yogpc.qp.neoforge.TransferNeoForge;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Required to access internal fields of {@link MachineStorage} in ItemStorageImpl and FluidStorageImpl for {@link ResourceHandler}.
 */
public final class MachineStorageNeoForge extends MachineStorage {
    public static class Factory implements MachineStorageFactory {
        @Override
        public MachineStorage createMachineStorage() {
            return new MachineStorageNeoForge();
        }
    }

    public static <T> ResourceHandler<ItemResource> createItemHandler(MachineStorageHolder<T> holder, T object) {
        return new ItemStorageImpl<>(holder, object);
    }

    public static <T> ResourceHandler<FluidResource> createFluidHandler(MachineStorageHolder<T> holder, T object) {
        return new FluidStorageImpl<>(holder, object);
    }

    private static final class ItemStorageImpl<T> extends SnapshotJournal<Object2LongLinkedOpenHashMap<ItemKey>> implements ResourceHandler<ItemResource> {
        private final MachineStorageHolder<T> holder;
        private final T object;

        private ItemStorageImpl(MachineStorageHolder<T> holder, T object) {
            this.holder = holder;
            this.object = object;
        }

        private MachineStorageNeoForge storage() {
            return (MachineStorageNeoForge) holder.getMachineStorage(object);
        }

        @Override
        public int size() {
            return storage().itemSlots() + 1; // Additional 1 for empty slot
        }

        @Override
        public ItemResource getResource(int index) {
            return ItemResource.of(storage().getItemByIndex(index));
        }

        @Override
        public long getAmountAsLong(int index) {
            return storage().getItemByIndex(index).getCount();
        }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            return Long.MAX_VALUE;
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return true;
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            updateSnapshots(transaction);
            storage().addItem(resource.toStack(amount));
            return amount;
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            updateSnapshots(transaction);
            return storage().extractItemByIndex(index, amount, true).getCount();
        }

        @Override
        protected Object2LongLinkedOpenHashMap<ItemKey> createSnapshot() {
            return storage().items.clone();
        }

        @Override
        protected void revertToSnapshot(Object2LongLinkedOpenHashMap<ItemKey> snapshot) {
            storage().items.clear();
            storage().items.putAll(snapshot);
        }
    }

    private static final class FluidStorageImpl<T> extends SnapshotJournal<Object2LongLinkedOpenHashMap<FluidKey>> implements ResourceHandler<FluidResource> {
        private final MachineStorageHolder<T> holder;
        private final T object;

        private FluidStorageImpl(MachineStorageHolder<T> holder, T object) {
            this.holder = holder;
            this.object = object;
        }

        private MachineStorageNeoForge storage() {
            return (MachineStorageNeoForge) holder.getMachineStorage(object);
        }

        @Override
        public int size() {
            return storage().fluidTanks() + 1; // Additional 1 for empty slot
        }

        @Override
        public FluidResource getResource(int index) {
            var fluidStackLike = storage().getFluidByIndex(index);
            if (fluidStackLike.isEmpty()) {
                return FluidResource.EMPTY;
            }
            return FluidResource.of(TransferNeoForge.toNeoForge(fluidStackLike));
        }

        @Override
        public long getAmountAsLong(int index) {
            return TransferNeoForge.toNeoForge(storage().getFluidByIndex(index)).getAmount();
        }

        @Override
        public long getCapacityAsLong(int index, FluidResource resource) {
            return Long.MAX_VALUE;
        }

        @Override
        public boolean isValid(int index, FluidResource resource) {
            return true;
        }

        @Override
        public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
            updateSnapshots(transaction);
            var fluidStack = resource.toStack(amount);
            var key = new FluidKey(fluidStack.getFluid(), fluidStack.getComponentsPatch());
            storage().fluids.addTo(key, TransferNeoForge.toCommonAmount(amount));
            return amount;
        }

        @Override
        public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
            updateSnapshots(transaction);
            var drained = storage().drainFluidByIndex(index, TransferNeoForge.toCommonAmount(amount), true);
            return TransferNeoForge.toNeoForge(drained).getAmount();
        }

        @Override
        protected Object2LongLinkedOpenHashMap<FluidKey> createSnapshot() {
            return storage().fluids.clone();
        }

        @Override
        protected void revertToSnapshot(Object2LongLinkedOpenHashMap<FluidKey> snapshot) {
            storage().fluids.clear();
            storage().fluids.putAll(snapshot);
        }
    }
}
