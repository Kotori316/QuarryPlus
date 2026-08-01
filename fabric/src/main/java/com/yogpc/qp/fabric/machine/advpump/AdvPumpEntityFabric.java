package com.yogpc.qp.fabric.machine.advpump;

import com.yogpc.qp.fabric.PlatformAccessFabric;
import com.yogpc.qp.machine.advpump.AdvPumpEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class AdvPumpEntityFabric extends AdvPumpEntity {

    public AdvPumpEntityFabric(BlockPos pos, BlockState blockState) {
        super(PlatformAccessFabric.RegisterObjectsFabric.ADV_PUMP_ENTITY_TYPE, pos, blockState);
    }

}
