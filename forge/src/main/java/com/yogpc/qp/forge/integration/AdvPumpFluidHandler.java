package com.yogpc.qp.forge.integration;

import com.yogpc.qp.forge.TransferForge;
import com.yogpc.qp.machine.MachineStorage;
import com.yogpc.qp.machine.MachineStorageHolder;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Exposes a machine's {@link MachineStorage} for external extraction only; insertion is always refused.
 */
public record AdvPumpFluidHandler<T>(MachineStorageHolder<T> holder,
                                     T object) implements IFluidHandler, ICapabilityProvider {
    private MachineStorage storage() {
        return holder.getMachineStorage(object);
    }

    @Override
    public int getTanks() {
        return storage().fluidTanks();
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return TransferForge.toForge(storage().getFluidByIndex(tank));
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
        var drained = storage().drainFluid(TransferForge.toCommon(resource), action.execute());
        return TransferForge.toForge(drained);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        var drained = storage().drainFluidByIndex(0, TransferForge.toCommonAmount(maxDrain), action.execute());
        return TransferForge.toForge(drained);
    }

    @Override
    public @NotNull <S> LazyOptional<S> getCapability(@NotNull Capability<S> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return ForgeCapabilities.FLUID_HANDLER.orEmpty(cap, LazyOptional.of(() -> this));
        }
        return LazyOptional.empty();
    }
}
