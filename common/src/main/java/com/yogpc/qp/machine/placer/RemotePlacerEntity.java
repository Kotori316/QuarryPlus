package com.yogpc.qp.machine.placer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

public final class RemotePlacerEntity extends AbstractPlacerTile {
    public static final String KEY_TARGET = "targetPos";
    @NotNull
    BlockPos targetPos;

    public RemotePlacerEntity(BlockPos pos, BlockState blockState) {
        super(pos, blockState);
        targetPos = pos.above();
    }

    @NotNull
    @Override
    protected BlockPos getTargetPos() {
        return targetPos;
    }

    @Override
    protected Direction getMachineFacing() {
        return Direction.UP;
    }

    @Override
    public void fromClientTag(ValueInput input) {
        super.fromClientTag(input);
        targetPos = input.read(KEY_TARGET, BlockPos.CODEC).orElseThrow();
    }

    @Override
    public ValueOutput toClientTag(ValueOutput output) {
        output.store(KEY_TARGET, BlockPos.CODEC, targetPos);
        return super.toClientTag(output);
    }
}
