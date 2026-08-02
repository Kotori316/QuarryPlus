package com.yogpc.qp.neoforge.integration;

import com.yogpc.qp.machine.MachineStorage;
import com.yogpc.qp.machine.MachineStorageHolder;
import com.yogpc.qp.neoforge.TransferNeoForge;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Exposes a machine's {@link MachineStorage} for external extraction only; insertion is always refused.
 */
public record AdvPumpFluidHandler<T>(MachineStorageHolder<T> holder, T object) implements IFluidHandler {
    private MachineStorage storage() {
        return holder.getMachineStorage(object);
    }

    @Override
    public int getTanks() {
        return storage().fluidTanks();
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return TransferNeoForge.toNeoForge(storage().getFluidByIndex(tank));
    }

    @Override
    public int getTankCapacity(int tank) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return false;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return 0;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        var drained = storage().drainFluid(TransferNeoForge.toCommon(resource), action.execute());
        return TransferNeoForge.toNeoForge(drained);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        var drained = storage().drainFluidByIndex(0, TransferNeoForge.toCommonAmount(maxDrain), action.execute());
        return TransferNeoForge.toNeoForge(drained);
    }
}
