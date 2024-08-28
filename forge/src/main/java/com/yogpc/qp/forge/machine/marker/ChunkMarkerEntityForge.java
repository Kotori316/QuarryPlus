package com.yogpc.qp.forge.machine.marker;

import com.yogpc.qp.machine.marker.ChunkMarkerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class ChunkMarkerEntityForge extends ChunkMarkerEntity {
    public ChunkMarkerEntityForge(BlockPos pos, BlockState blockState) {
        super(pos, blockState);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return getRenderAabb();
    }
}
