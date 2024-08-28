package com.yogpc.qp.neoforge.render;

import com.yogpc.qp.machine.marker.FlexibleMarkerEntity;
import com.yogpc.qp.render.RenderFlexibleMarker;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

public final class RenderFlexibleMarkerNeoForge extends RenderFlexibleMarker {

    public RenderFlexibleMarkerNeoForge(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public AABB getRenderBoundingBox(FlexibleMarkerEntity blockEntity) {
        return blockEntity.getRenderAabb();
    }
}
