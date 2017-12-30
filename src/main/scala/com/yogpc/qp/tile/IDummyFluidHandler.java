package com.yogpc.qp.tile;

import javax.annotation.Nullable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public interface IDummyFluidHandler extends IFluidHandler {

    @Override
    default int fill(FluidStack resource, boolean doFill) {
        return 0;
    }

    @Nullable
    @Override
    default FluidStack drain(FluidStack resource, boolean doDrain) {
        return null;
    }

    @Nullable
    @Override
    default FluidStack drain(int maxDrain, boolean doDrain) {
        return null;
    }
}
