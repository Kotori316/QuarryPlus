package com.yogpc.qp.neoforge.render;

import com.yogpc.qp.machine.quarry.QuarryEntity;
import com.yogpc.qp.render.RenderQuarry;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

public final class RenderQuarryNeoForge extends RenderQuarry {
    public RenderQuarryNeoForge(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public AABB getRenderBoundingBox(QuarryEntity blockEntity) {
        var aabb = blockEntity.getRenderAabb();
        if (aabb == null) {
            return super.getRenderBoundingBox(blockEntity);
        } else {
            return aabb;
        }
    }
}
