package com.yogpc.qp.compat;

import java.util.Optional;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class FluidStore {
    public static final boolean enabled;
    public static final long AMOUNT = 1000;

    static {
        enabled = true;//ModList.get().isLoaded("fluidtank");
    }

    public static void injectToNearTile(World world, BlockPos pos, FluidStack fluid) {
        injectToNearTile(world, pos, fluid, AMOUNT);
    }

    /**
     * @return inserted amount.
     */
    public static long injectToNearTile(World world, BlockPos pos, FluidStack fluid, long amount) {
        if (!enabled) return 0;
        return injectToNearTile_internal(world, pos, fluid, amount);
    }

    private static long injectToNearTile_internal(World world, BlockPos pos, FluidStack fluid, long amount) {
        FluidStack fluidKind = new FluidStack(fluid, (int) Math.max(Math.min(amount, Integer.MAX_VALUE), 0));
        for (EnumFacing facing : EnumFacing.values()) {
            TileEntity entity = world.getTileEntity(pos.offset(facing));
            if (entity != null) {
                Optional<IFluidHandler> capability = Optional.ofNullable(entity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite()));
                capability.ifPresent(tank -> {
                    int filled = tank.fill(fluidKind, false);
                    if (filled > 0) {
                        tank.fill(fluidKind, true);
                        fluidKind.amount -= filled;
                    }
                });
                if (fluidKind.amount <= 0) break;
            }
        }
        return amount - fluidKind.amount;
    }

}
