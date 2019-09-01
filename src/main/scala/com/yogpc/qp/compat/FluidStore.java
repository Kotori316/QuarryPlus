package com.yogpc.qp.compat;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class FluidStore {
    /**
     * @return inserted amount.
     */
    public static int injectToNearTile(World world, BlockPos pos, FluidStack fluid) {
        return injectToNearTile_internal(world, pos, fluid);
    }

    private static int injectToNearTile_internal(World world, BlockPos pos, FluidStack fluid) {
        if (fluid.isEmpty()) {
            return 0;
        }
        int source = fluid.getAmount();
        for (Direction facing : Direction.values()) {
            LazyOptional<IFluidHandler> lazyOptional = FluidUtil.getFluidHandler(world, pos.offset(facing), facing.getOpposite());
            lazyOptional.ifPresent(handler -> {
                int fill = handler.fill(fluid, IFluidHandler.FluidAction.SIMULATE);
                if (fill > 0) {
                    int filled = handler.fill(fluid, IFluidHandler.FluidAction.EXECUTE);
                    fluid.setAmount(fluid.getAmount() - filled);
                }
            });
            if (fluid.isEmpty()) {
                return source;
            }
        }
        return source - fluid.getAmount();
    }

}
