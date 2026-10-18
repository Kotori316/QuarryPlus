package com.yogpc.qp.neoforge.integration;

import com.yogpc.qp.machine.MachineStorageHolder;
import com.yogpc.qp.neoforge.machine.MachineStorageNeoForge;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Exposes a machine's {@link com.yogpc.qp.machine.MachineStorage} for external extraction only; insertion is always refused.
 */
public final class AdvPumpFluidHandler<T> implements ResourceHandler<FluidResource> {
    private final ResourceHandler<FluidResource> delegate;

    public AdvPumpFluidHandler(MachineStorageHolder<T> holder, T object) {
        this.delegate = MachineStorageNeoForge.createFluidHandler(holder, object);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public FluidResource getResource(int index) {
        return delegate.getResource(index);
    }

    @Override
    public long getAmountAsLong(int index) {
        return delegate.getAmountAsLong(index);
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
        return delegate.getCapacityAsLong(index, resource);
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        return delegate.isValid(index, resource);
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        return delegate.extract(index, resource, amount, transaction);
    }
}
