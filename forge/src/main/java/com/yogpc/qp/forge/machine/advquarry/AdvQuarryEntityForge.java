package com.yogpc.qp.forge.machine.advquarry;

import com.yogpc.qp.forge.PlatformAccessForge;
import com.yogpc.qp.forge.machine.QuarryFakePlayer;
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

public final class AdvQuarryEntityForge extends AdvQuarryEntity {
    public AdvQuarryEntityForge(BlockPos pos, BlockState blockState) {
        super(PlatformAccessForge.RegisterObjectsForge.ADV_QUARRY_ENTITY_TYPE.get(), pos, blockState);
    }

    @Override
    protected BlockBreakEventResult checkBreakEvent(Level level, ServerPlayer fakePlayer, BlockState state, BlockPos target, @Nullable BlockEntity blockEntity) {
        var breakEvent = new BlockEvent.BreakEvent(level, target, state, fakePlayer);
        MinecraftForge.EVENT_BUS.post(breakEvent);
        return new BlockBreakEventResult(breakEvent.isCanceled(), OptionalInt.of(breakEvent.getExpToDrop()));
    }

    @Override
    protected BlockBreakEventResult afterBreak(Level level, ServerPlayer fakePlayer, BlockState state, BlockPos target, @Nullable BlockEntity blockEntity, List<ItemStack> drops, ItemStack pickaxe, BlockState newState) {
        level.setBlock(target, newState, Block.UPDATE_CLIENTS);
        // Forge doesn't have after break event
        return BlockBreakEventResult.EMPTY;
    }

    @Override
    protected ServerPlayer getQuarryFakePlayer(ServerLevel level, BlockPos target) {
        var player = QuarryFakePlayer.get(level);
        QuarryFakePlayerCommon.setDirection(player, Direction.DOWN);
        return player;
    }

}
