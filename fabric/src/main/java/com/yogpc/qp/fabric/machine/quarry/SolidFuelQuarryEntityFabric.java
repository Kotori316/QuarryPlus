package com.yogpc.qp.fabric.machine.quarry;

import com.yogpc.qp.fabric.PlatformAccessFabric;
import com.yogpc.qp.machine.quarry.SolidFuelQuarryEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class SolidFuelQuarryEntityFabric extends SolidFuelQuarryEntity {
    public SolidFuelQuarryEntityFabric(BlockPos pos, BlockState blockState) {
        super(PlatformAccessFabric.RegisterObjectsFabric.SOLID_FUEL_QUARRY_ENTITY_TYPE, pos, blockState);
    }
}
