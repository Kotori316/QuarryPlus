package com.yogpc.qp.compat;

import net.minecraft.fluid.Fluid;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.ModList;

import com.kotori316.fluidtank.FluidAmount;

public class FluidStore {
    public static final boolean enabled;
    public static final long AMOUNT = 1000;

    static {
        enabled = ModList.get().isLoaded("fluidtank");
    }

    public static void injectToNearTile(World world, BlockPos pos, Fluid fluid) {
        injectToNearTile(world, pos, fluid, AMOUNT);
    }

    /**
     * @return inserted amount.
     */
    public static long injectToNearTile(World world, BlockPos pos, Fluid fluid, long amount) {
        if (!enabled) return 0;
        return injectToNearTile_internal(world, pos, fluid, amount);
    }

    private static long injectToNearTile_internal(World world, BlockPos pos, Fluid fluidKind, long amount) {
        if (TANK_CAPABILITY == null) return 0;
        FluidAmount[] fluidAmounts = new FluidAmount[]{FluidAmount.apply(fluidKind, amount)};
        for (EnumFacing facing : EnumFacing.values()) {
            TileEntity entity = world.getTileEntity(pos.offset(facing));
            if (entity != null) {
                LazyOptional<FluidAmount.Tank> capability = entity.getCapability(TANK_CAPABILITY, facing.getOpposite());
                capability.ifPresent(tank -> {
                    FluidAmount filled = tank.fill(fluidAmounts[0], false, 0);
                    if (filled.nonEmpty()) {
                        tank.fill(fluidAmounts[0], true, 0);
                        fluidAmounts[0] = fluidAmounts[0].$minus(filled);
                    }
                });
                if (fluidAmounts[0].isEmpty()) break;
            }
        }
        return amount - fluidAmounts[0].amount();
    }

    @CapabilityInject(FluidAmount.Tank.class)
    public static Capability<FluidAmount.Tank> TANK_CAPABILITY = null;

}
