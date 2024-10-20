package com.yogpc.qp.neoforge.machine.quarry;

import com.yogpc.qp.machine.quarry.QuarryEntity;
import com.yogpc.qp.neoforge.PlatformAccessNeoForge;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class QuarryEntityNeoForge extends QuarryEntity {
    public QuarryEntityNeoForge(BlockPos pos, BlockState blockState) {
        super(PlatformAccessNeoForge.RegisterObjectsNeoForge.QUARRY_ENTITY_TYPE.get(), pos, blockState);
    }
}
