package com.yogpc.qp.neoforge.machine.quarry;

import com.yogpc.qp.machine.quarry.SolidFuelQuarryEntity;
import com.yogpc.qp.neoforge.PlatformAccessNeoForge;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class SolidFuelQuarryEntityNeoForge extends SolidFuelQuarryEntity {
    public SolidFuelQuarryEntityNeoForge(BlockPos pos, BlockState blockState) {
        super(PlatformAccessNeoForge.RegisterObjectsNeoForge.SOLID_FUEL_QUARRY_ENTITY_TYPE.get(), pos, blockState);
    }
}
