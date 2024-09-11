package com.yogpc.qp.neoforge.machine.advquarry;

import com.yogpc.qp.machine.advquarry.AdvQuarryEntity;
import com.yogpc.qp.neoforge.PlatformAccessNeoForge;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class AdvQuarryEntityNeoForge extends AdvQuarryEntity {

    public AdvQuarryEntityNeoForge(BlockPos pos, BlockState blockState) {
        super(PlatformAccessNeoForge.RegisterObjectsNeoForge.ADV_QUARRY_ENTITY_TYPE.get(), pos, blockState);
    }
}
