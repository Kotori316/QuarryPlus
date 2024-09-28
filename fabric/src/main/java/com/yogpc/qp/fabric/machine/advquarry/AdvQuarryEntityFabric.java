package com.yogpc.qp.fabric.machine.advquarry;

import com.yogpc.qp.fabric.PlatformAccessFabric;
import com.yogpc.qp.machine.QuarryFakePlayerCommon;
import com.yogpc.qp.machine.advquarry.AdvQuarryEntity;
import com.yogpc.qp.machine.misc.BlockBreakEventResult;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.OptionalInt;

public final class AdvQuarryEntityFabric extends AdvQuarryEntity {
    public AdvQuarryEntityFabric(BlockPos pos, BlockState blockState) {
        super(PlatformAccessFabric.RegisterObjectsFabric.ADV_QUARRY_ENTITY_TYPE, pos, blockState);
    }

    @Override
    protected BlockBreakEventResult checkBreakEvent(Level level, ServerPlayer fakePlayer, BlockState state, BlockPos target, @Nullable BlockEntity blockEntity) {
        boolean result = PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(level, fakePlayer, target, state, blockEntity);
        if (!result) {
            // cancelled
            PlayerBlockBreakEvents.CANCELED.invoker().onBlockBreakCanceled(level, fakePlayer, target, state, blockEntity);
            return BlockBreakEventResult.CANCELED;
        }
        return new BlockBreakEventResult(false, OptionalInt.of(0));
    }

    @Override
    protected BlockBreakEventResult afterBreak(Level level, ServerPlayer fakePlayer, BlockState state, BlockPos target, @Nullable BlockEntity blockEntity, List<ItemStack> drops, ItemStack pickaxe, BlockState newState) {
        state.spawnAfterBreak((ServerLevel) level, target, pickaxe, true);
        level.setBlock(target, newState, Block.UPDATE_ALL);
        PlayerBlockBreakEvents.AFTER.invoker().afterBlockBreak(level, fakePlayer, target, state, blockEntity);
        return BlockBreakEventResult.EMPTY;
    }

    @Override
    protected ServerPlayer getQuarryFakePlayer(ServerLevel level, BlockPos target) {
        var fakePlayer = FakePlayer.get(level, QuarryFakePlayerCommon.PROFILE);
        QuarryFakePlayerCommon.setDirection(fakePlayer, Direction.DOWN);
        return fakePlayer;
    }
}
