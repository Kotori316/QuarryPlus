package com.yogpc.qp.machines.base;

import javax.annotation.Nonnull;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public interface IDummyFluidHandler extends IFluidHandler {
    @Override
    default boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
        return true;
    }

    @Override
    default int fill(FluidStack resource, FluidAction action) {
        return 0;
    }

    @Nonnull
    @Override
    default FluidStack drain(FluidStack resource, FluidAction action) {
        return FluidStack.EMPTY;
    }

    @Nonnull
    @Override
    default FluidStack drain(int maxDrain, FluidAction action) {
        return FluidStack.EMPTY;
    }
}
