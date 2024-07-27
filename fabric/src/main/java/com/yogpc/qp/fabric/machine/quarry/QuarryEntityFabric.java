package com.yogpc.qp.fabric.machine.quarry;

import com.yogpc.qp.fabric.PlatformAccessFabric;
import com.yogpc.qp.machine.QuarryFakePlayerCommon;
import com.yogpc.qp.machine.quarry.QuarryEntity;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class QuarryEntityFabric extends QuarryEntity {
    public QuarryEntityFabric(BlockPos pos, BlockState blockState) {
        super(PlatformAccessFabric.RegisterObjectsFabric.QUARRY_ENTITY_TYPE, pos, blockState);
    }

    @Override
    protected boolean checkBreakEvent(Level level, ServerPlayer fakePlayer, BlockState state, BlockPos target, @Nullable BlockEntity blockEntity) {
        boolean result = PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(level, fakePlayer, target, state, blockEntity);
        if (!result) {
            // cancelled
            PlayerBlockBreakEvents.CANCELED.invoker().onBlockBreakCanceled(level, fakePlayer, target, state, blockEntity);
            return true;
        }
        return false;
    }

    @Override
    protected void afterBreak(Level level, ServerPlayer fakePlayer, BlockState state, BlockPos target, @Nullable BlockEntity blockEntity) {
        PlayerBlockBreakEvents.AFTER.invoker().afterBlockBreak(level, fakePlayer, target, state, blockEntity);
    }

    @Override
    protected ServerPlayer getQuarryFakePlayer(ServerLevel level, BlockPos target) {
        var fakePlayer = FakePlayer.get(level, QuarryFakePlayerCommon.PROFILE);
        QuarryFakePlayerCommon.setDirection(fakePlayer, Direction.DOWN);
        return fakePlayer;
    }
}
