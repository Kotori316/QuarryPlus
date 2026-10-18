package com.yogpc.qp.neoforge.machine.advpump;

import com.yogpc.qp.machine.advpump.AdvPumpEntity;
import com.yogpc.qp.neoforge.PlatformAccessNeoForge;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class AdvPumpEntityNeoForge extends AdvPumpEntity {

    public AdvPumpEntityNeoForge(BlockPos pos, BlockState blockState) {
        super(PlatformAccessNeoForge.RegisterObjectsNeoForge.ADV_PUMP_ENTITY_TYPE.get(), pos, blockState);
    }

}
