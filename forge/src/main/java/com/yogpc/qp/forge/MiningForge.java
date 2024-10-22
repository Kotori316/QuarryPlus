package com.yogpc.qp.forge;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.forge.machine.QuarryFakePlayer;
import com.yogpc.qp.machine.QpEntity;
import com.yogpc.qp.machine.QuarryFakePlayerCommon;
import com.yogpc.qp.machine.advquarry.AdvQuarryEntity;
import com.yogpc.qp.machine.misc.BlockBreakEventResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.OptionalInt;

final class MiningForge implements PlatformAccess.Mining {
    @Override
    public BlockBreakEventResult checkBreakEvent(QpEntity miningEntity, Level level, ServerPlayer fakePlayer, BlockState state, BlockPos target, @Nullable BlockEntity blockEntity) {
        var breakEvent = new BlockEvent.BreakEvent(level, target, state, fakePlayer);
        MinecraftForge.EVENT_BUS.post(breakEvent);
        return new BlockBreakEventResult(breakEvent.isCanceled(), OptionalInt.of(breakEvent.getExpToDrop()), List.of());
    }

    @Override
    public BlockBreakEventResult afterBreak(QpEntity miningEntity, Level level, ServerPlayer fakePlayer, BlockState state, BlockPos target, @Nullable BlockEntity blockEntity, List<ItemStack> drops, ItemStack pickaxe, BlockState newState) {
        int updateFlag = switch (miningEntity) {
            case AdvQuarryEntity ignore -> Block.UPDATE_CLIENTS;
            case null, default -> Block.UPDATE_ALL;
        };
        level.setBlock(target, newState, updateFlag);
        // Forge doesn't have after break event
        return BlockBreakEventResult.empty(drops);
    }

    @Override
    public ServerPlayer getQuarryFakePlayer(QpEntity miningEntity, ServerLevel level, BlockPos target) {
        var player = QuarryFakePlayer.get(level);
        QuarryFakePlayerCommon.setDirection(player, Direction.DOWN);
        return player;
    }
}
