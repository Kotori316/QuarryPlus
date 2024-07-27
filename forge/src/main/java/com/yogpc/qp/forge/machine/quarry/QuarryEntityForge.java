package com.yogpc.qp.forge.machine.quarry;

import com.yogpc.qp.forge.PlatformAccessForge;
import com.yogpc.qp.machine.quarry.QuarryEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class QuarryEntityForge extends QuarryEntity {
    public QuarryEntityForge(BlockPos pos, BlockState blockState) {
        super(PlatformAccessForge.RegisterObjectsForge.QUARRY_ENTITY_TYPE.get(), pos, blockState);
    }

    @Override
    protected boolean checkBreakEvent(Level level, ServerPlayer fakePlayer, BlockState state, BlockPos target, @Nullable BlockEntity blockEntity) {
        return false;
    }

    @Override
    protected void afterBreak(Level level, ServerPlayer fakePlayer, BlockState state, BlockPos target, @Nullable BlockEntity blockEntity) {

    }

    @Override
    protected ServerPlayer getQuarryFakePlayer(ServerLevel level, BlockPos target) {
        return null;
    }
}
