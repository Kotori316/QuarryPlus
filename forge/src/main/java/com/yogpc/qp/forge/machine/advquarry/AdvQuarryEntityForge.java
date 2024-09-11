package com.yogpc.qp.forge.machine.advquarry;

import com.yogpc.qp.forge.PlatformAccessForge;
import com.yogpc.qp.machine.advquarry.AdvQuarryEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class AdvQuarryEntityForge extends AdvQuarryEntity {
    public AdvQuarryEntityForge(BlockPos pos, BlockState blockState) {
        super(PlatformAccessForge.RegisterObjectsForge.ADV_QUARRY_ENTITY_TYPE.get(), pos, blockState);
    }
}
