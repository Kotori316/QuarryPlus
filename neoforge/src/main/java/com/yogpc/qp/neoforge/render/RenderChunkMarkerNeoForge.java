package com.yogpc.qp.neoforge.render;

import com.yogpc.qp.machine.marker.ChunkMarkerEntity;
import com.yogpc.qp.render.RenderChunkMarker;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

public final class RenderChunkMarkerNeoForge extends RenderChunkMarker {
    public RenderChunkMarkerNeoForge(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public AABB getRenderBoundingBox(ChunkMarkerEntity blockEntity) {
        return blockEntity.getRenderAabb();
    }
}
