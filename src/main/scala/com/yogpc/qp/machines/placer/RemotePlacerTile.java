package com.yogpc.qp.machines.placer;

import com.yogpc.qp.Holder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public final class RemotePlacerTile extends PlacerTile {
    BlockPos targetPos;

    public RemotePlacerTile(BlockPos pos, BlockState state) {
        super(Holder.REMOTE_PLACER_TYPE, pos, state);
        this.targetPos = pos.above();
    }

    @Override
    protected BlockPos getTargetPos() {
        return this.targetPos;
    }

    @Override
    protected Direction getMachineFacing() {
        return Direction.UP;
    }

    @Override
    protected boolean isPowered() {
        return PlacerBlock.isPoweredToWork(level, getBlockPos(), null);
    }
}
