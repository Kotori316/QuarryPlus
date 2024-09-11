package com.yogpc.qp.machine.advquarry;

import com.yogpc.qp.machine.PowerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AdvQuarryEntity extends PowerEntity {
    protected AdvQuarryEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }
}
