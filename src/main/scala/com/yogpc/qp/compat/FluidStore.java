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

    static {
        enabled = ModList.get().isLoaded("fluidtank");
    }

    public static void injectToNearTile(World world, BlockPos pos, Fluid fluid) {
        if (!enabled) return;
        injectToNearTile_internal(world, pos, fluid);
    }

    private static void injectToNearTile_internal(World world, BlockPos pos, Fluid fluid) {
        if (TANK_CAPABILITY == null) return;
        FluidAmount[] amount = new FluidAmount[]{FluidAmount.apply(fluid, FluidAmount.AMOUNT_BUCKET())};
        for (EnumFacing facing : EnumFacing.values()) {
            TileEntity entity = world.getTileEntity(pos.offset(facing));
            if (entity != null) {
                LazyOptional<FluidAmount.Tank> capability = entity.getCapability(TANK_CAPABILITY, facing.getOpposite());
                capability.ifPresent(tank -> {
                    FluidAmount filled = tank.fill(amount[0], false, 0);
                    if (filled.nonEmpty()) {
                        tank.fill(amount[0], true, 0);
                        amount[0] = amount[0].$minus(filled);
                    }
                });
                if (amount[0].isEmpty()) break;
            }
        }
    }
    @CapabilityInject(FluidAmount.Tank.class)
    public static Capability<FluidAmount.Tank> TANK_CAPABILITY = null;

}
