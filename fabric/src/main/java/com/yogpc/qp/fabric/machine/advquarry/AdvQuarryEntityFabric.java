package com.yogpc.qp.fabric.machine.advquarry;

import com.yogpc.qp.fabric.PlatformAccessFabric;
import com.yogpc.qp.machine.advquarry.AdvQuarryEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class AdvQuarryEntityFabric extends AdvQuarryEntity {
    public AdvQuarryEntityFabric(BlockPos pos, BlockState blockState) {
        super(PlatformAccessFabric.RegisterObjectsFabric.ADV_QUARRY_ENTITY_TYPE, pos, blockState);
    }
}
