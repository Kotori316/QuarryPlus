package com.yogpc.qp.neoforge.machine.advquarry;

import com.yogpc.qp.machine.QuarryFakePlayerCommon;
import com.yogpc.qp.machine.advquarry.AdvQuarryEntity;
import com.yogpc.qp.machine.misc.BlockBreakEventResult;
import com.yogpc.qp.neoforge.PlatformAccessNeoForge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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

public final class AdvQuarryEntityNeoForge extends AdvQuarryEntity {

    public AdvQuarryEntityNeoForge(BlockPos pos, BlockState blockState) {
        super(PlatformAccessNeoForge.RegisterObjectsNeoForge.ADV_QUARRY_ENTITY_TYPE.get(), pos, blockState);
    }

    @Override
    protected BlockBreakEventResult checkBreakEvent(Level level, ServerPlayer fakePlayer, BlockState state, BlockPos target, @Nullable BlockEntity blockEntity) {
        var breakEvent = new BlockEvent.BreakEvent(level, target, state, fakePlayer);
        NeoForge.EVENT_BUS.post(breakEvent);
        return new BlockBreakEventResult(breakEvent.isCanceled(), OptionalInt.empty());
    }

    @Override
    protected BlockBreakEventResult afterBreak(Level level, ServerPlayer fakePlayer, BlockState state, BlockPos target, @Nullable BlockEntity blockEntity, List<ItemStack> drops, ItemStack pickaxe, BlockState newState) {
        // Just use BlockDropsEvent to check exp
        var dropEvent = new BlockDropsEvent((ServerLevel) level, target, state, blockEntity, new ArrayList<>(), fakePlayer, pickaxe);
        NeoForge.EVENT_BUS.post(dropEvent);
        level.setBlock(target, newState, Block.UPDATE_ALL);
        OptionalInt exp = dropEvent.isCanceled() ? OptionalInt.empty() : OptionalInt.of(dropEvent.getDroppedExperience());
        return new BlockBreakEventResult(dropEvent.isCanceled(), exp);
    }

    @Override
    protected ServerPlayer getQuarryFakePlayer(ServerLevel level, BlockPos target) {
        var player = FakePlayerFactory.get(level, QuarryFakePlayerCommon.PROFILE);
        QuarryFakePlayerCommon.setDirection(player, Direction.DOWN);
        return player;
    }
}
