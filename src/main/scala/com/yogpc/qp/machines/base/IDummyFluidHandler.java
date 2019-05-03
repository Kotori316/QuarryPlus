package com.yogpc.qp.machines.base;

import javax.annotation.Nullable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;

public interface IDummyFluidHandler extends IFluidHandler {

    IFluidTankProperties[] emptyPropertyArray = EmptyFluidHandler.EMPTY_TANK_PROPERTIES_ARRAY;

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
