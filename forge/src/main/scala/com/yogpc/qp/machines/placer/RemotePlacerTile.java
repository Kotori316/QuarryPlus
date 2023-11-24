package com.yogpc.qp.machines.placer;

import com.yogpc.qp.Holder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public final class RemotePlacerTile extends PlacerTile {
    public static final String KEY_TARGET = "targetPos";
    BlockPos targetPos;

    public RemotePlacerTile(BlockPos pos, BlockState state) {
        super(Holder.REMOTE_PLACER_TYPE, pos, state);
        this.targetPos = pos.above();
    }

    @Override
    protected void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        compound.putLong(KEY_TARGET, this.targetPos.asLong());
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        if (compound.contains(KEY_TARGET, CompoundTag.TAG_LONG))
            this.targetPos = BlockPos.of(compound.getLong(KEY_TARGET));
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
