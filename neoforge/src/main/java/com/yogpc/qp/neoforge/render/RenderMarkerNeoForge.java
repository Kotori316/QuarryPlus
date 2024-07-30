package com.yogpc.qp.neoforge.render;

import com.yogpc.qp.machine.marker.NormalMarkerEntity;
import com.yogpc.qp.render.RenderMarker;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

public final class RenderMarkerNeoForge extends RenderMarker {
    public RenderMarkerNeoForge(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public AABB getRenderBoundingBox(NormalMarkerEntity blockEntity) {
        var aabb = blockEntity.getRenderAabb();
        if (aabb != null) {
            return aabb;
        }
        return super.getRenderBoundingBox(blockEntity);
    }
}
