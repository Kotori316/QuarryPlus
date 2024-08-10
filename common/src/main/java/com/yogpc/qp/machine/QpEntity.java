package com.yogpc.qp.machine;

import com.yogpc.qp.PlatformAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class QpEntity extends BlockEntity {
    public final boolean enabled;

    protected QpEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        this.enabled = PlatformAccess.config().enableMap().enabled(type);
    }
}
