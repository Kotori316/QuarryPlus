package com.yogpc.qp.forge.machine.quarry;

import com.yogpc.qp.forge.PlatformAccessForge;
import com.yogpc.qp.machine.quarry.SolidFuelQuarryEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class SolidFuelQuarryEntityForge extends SolidFuelQuarryEntity {
    public SolidFuelQuarryEntityForge(BlockPos pos, BlockState blockState) {
        super(PlatformAccessForge.RegisterObjectsForge.SOLID_FUEL_QUARRY_ENTITY_TYPE.get(), pos, blockState);
    }

    @Override
    public AABB getRenderBoundingBox() {
        var aabb = getRenderAabb();
        if (aabb == null) {
            return super.getRenderBoundingBox();
        } else {
            return aabb;
        }
    }
}
