package com.yogpc.qp.neoforge;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.machine.QpEntity;
import com.yogpc.qp.machine.QuarryFakePlayerCommon;
import com.yogpc.qp.machine.advquarry.AdvQuarryEntity;
import com.yogpc.qp.machine.misc.BlockBreakEventResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;

final class MiningNeoForge implements PlatformAccess.Mining {
    @Override
    public BlockBreakEventResult checkBreakEvent(QpEntity miningEntity, Level level, ServerPlayer fakePlayer, BlockState state, BlockPos target, @Nullable BlockEntity blockEntity) {
        var breakEvent = new BlockEvent.BreakEvent(level, target, state, fakePlayer);
        NeoForge.EVENT_BUS.post(breakEvent);
        return new BlockBreakEventResult(breakEvent.isCanceled(), OptionalInt.empty(), List.of());
    }

    @Override
    public BlockBreakEventResult afterBreak(QpEntity miningEntity, Level level, ServerPlayer fakePlayer, BlockState state, BlockPos target, @Nullable BlockEntity blockEntity, List<ItemStack> drops, ItemStack pickaxe, BlockState newState) {
        int updateFlag = switch (miningEntity) {
            case AdvQuarryEntity ignore -> Block.UPDATE_CLIENTS;
            case null, default -> Block.UPDATE_ALL;
        };
        // Just use BlockDropsEvent to check exp
        var itemDropEntities = drops.stream().map(i -> new ItemEntity(level, target.getX(), target.getY(), target.getZ(), i)).collect(Collectors.toCollection(ArrayList::new));
        var dropEvent = new BlockDropsEvent((ServerLevel) level, target, state, blockEntity, itemDropEntities, fakePlayer, pickaxe);
        NeoForge.EVENT_BUS.post(dropEvent);
        level.setBlock(target, newState, updateFlag);
        OptionalInt exp = dropEvent.isCanceled() ? OptionalInt.empty() : OptionalInt.of(dropEvent.getDroppedExperience());
        return new BlockBreakEventResult(dropEvent.isCanceled(), exp, itemDropEntities.stream().map(ItemEntity::getItem).toList());
    }

    @Override
    public ServerPlayer getQuarryFakePlayer(QpEntity miningEntity, ServerLevel level, BlockPos target) {
        var player = FakePlayerFactory.get(level, QuarryFakePlayerCommon.PROFILE);
        QuarryFakePlayerCommon.setDirection(player, Direction.DOWN);
        return player;
    }
}
