package com.yogpc.qp.tile;

import javax.annotation.Nullable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public interface IDummyFluidHandler extends IFluidHandler {

    public static final IFluidTankProperties[] emptyPropertyArray =
        new IFluidTankProperties[]{new FluidTankProperties(null, 0, false, false)};

    @Override
    IFluidTankProperties[] getTankProperties();

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
