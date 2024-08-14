package com.yogpc.qp.machine;

import com.yogpc.qp.PlatformAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class QpEntity extends BlockEntity {
    public final boolean enabled;

    protected QpEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        this.enabled = PlatformAccess.config().enableMap().enabled(getMachineName(type));
    }

    protected String getMachineName(BlockEntityType<?> type) {
        var key = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type);
        if (key == null) {
            // This block is not registered
            return "invalid";
        }
        return key.getPath();
    }

}
