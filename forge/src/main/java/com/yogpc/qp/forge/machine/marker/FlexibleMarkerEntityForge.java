package com.yogpc.qp.forge.machine.marker;

import com.yogpc.qp.machine.marker.FlexibleMarkerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

public final class FlexibleMarkerEntityForge extends FlexibleMarkerEntity {
    public FlexibleMarkerEntityForge(@NotNull BlockPos pos, BlockState blockState) {
        super(pos, blockState);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return getRenderAabb();
    }
}
