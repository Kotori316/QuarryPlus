package com.yogpc.qp.neoforge.machine.quarry;

import com.yogpc.qp.machine.QuarryFakePlayerCommon;
import com.yogpc.qp.machine.quarry.QuarryEntity;
import com.yogpc.qp.neoforge.PlatformAccessNeoForge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.jetbrains.annotations.Nullable;

public final class QuarryEntityNeoForge extends QuarryEntity {
    public QuarryEntityNeoForge(BlockPos pos, BlockState blockState) {
        super(PlatformAccessNeoForge.RegisterObjectsNeoForge.QUARRY_ENTITY_TYPE.get(), pos, blockState);
    }

    @Override
    protected boolean checkBreakEvent(Level level, ServerPlayer fakePlayer, BlockState state, BlockPos target, @Nullable BlockEntity blockEntity) {
        var breakEvent = new BlockEvent.BreakEvent(level, target, state, fakePlayer);
        NeoForge.EVENT_BUS.post(breakEvent);
        return breakEvent.isCanceled();
    }

    @Override
    protected void afterBreak(Level level, ServerPlayer fakePlayer, BlockState state, BlockPos target, @Nullable BlockEntity blockEntity) {
        // Forge doesn't have after break event
    }

    @Override
    protected ServerPlayer getQuarryFakePlayer(ServerLevel level, BlockPos target) {
        var player = FakePlayerFactory.get(level, QuarryFakePlayerCommon.PROFILE);
        QuarryFakePlayerCommon.setDirection(player, Direction.DOWN);
        return player;
    }
}
