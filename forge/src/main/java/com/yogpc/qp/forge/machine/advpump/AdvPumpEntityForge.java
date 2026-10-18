package com.yogpc.qp.forge.machine.advpump;

import com.yogpc.qp.forge.PlatformAccessForge;
import com.yogpc.qp.machine.advpump.AdvPumpEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class AdvPumpEntityForge extends AdvPumpEntity {

    public AdvPumpEntityForge(BlockPos pos, BlockState blockState) {
        super(PlatformAccessForge.RegisterObjectsForge.ADV_PUMP_ENTITY_TYPE.get(), pos, blockState);
    }

}
