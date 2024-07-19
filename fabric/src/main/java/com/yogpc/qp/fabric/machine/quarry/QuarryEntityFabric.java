package com.yogpc.qp.fabric.machine.quarry;

import com.yogpc.qp.fabric.PlatformAccessFabric;
import com.yogpc.qp.machine.quarry.QuarryEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class QuarryEntityFabric extends QuarryEntity {
    public QuarryEntityFabric(BlockPos pos, BlockState blockState) {
        super(PlatformAccessFabric.RegisterObjectsFabric.QUARRY_ENTITY_TYPE, pos, blockState);
    }
}
