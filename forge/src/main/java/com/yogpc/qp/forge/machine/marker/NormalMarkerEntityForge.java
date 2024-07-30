package com.yogpc.qp.forge.machine.marker;

import com.yogpc.qp.machine.marker.NormalMarkerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class NormalMarkerEntityForge extends NormalMarkerEntity {
    public NormalMarkerEntityForge(BlockPos pos, BlockState blockState) {
        super(pos, blockState);
    }

    @Override
    public AABB getRenderBoundingBox() {
        var aabb = getRenderAabb();
        if (aabb == null) {
            return super.getRenderBoundingBox();
        }
        return aabb;
    }
}
