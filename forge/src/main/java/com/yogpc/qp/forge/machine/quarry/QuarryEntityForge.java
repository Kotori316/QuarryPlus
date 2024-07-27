package com.yogpc.qp.forge.machine.quarry;

import com.yogpc.qp.forge.PlatformAccessForge;
import com.yogpc.qp.forge.machine.QuarryFakePlayer;
import com.yogpc.qp.machine.QuarryFakePlayerCommon;
import com.yogpc.qp.machine.quarry.QuarryEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import org.jetbrains.annotations.Nullable;

public final class QuarryEntityForge extends QuarryEntity {
    public QuarryEntityForge(BlockPos pos, BlockState blockState) {
        super(PlatformAccessForge.RegisterObjectsForge.QUARRY_ENTITY_TYPE.get(), pos, blockState);
    }

    @Override
    protected boolean checkBreakEvent(Level level, ServerPlayer fakePlayer, BlockState state, BlockPos target, @Nullable BlockEntity blockEntity) {
        var breakEvent = new BlockEvent.BreakEvent(level, target, state, fakePlayer);
        MinecraftForge.EVENT_BUS.post(breakEvent);
        return breakEvent.isCanceled();
    }

    @Override
    protected void afterBreak(Level level, ServerPlayer fakePlayer, BlockState state, BlockPos target, @Nullable BlockEntity blockEntity) {
        // Forge doesn't have after break event
    }

    @Override
    protected ServerPlayer getQuarryFakePlayer(ServerLevel level, BlockPos target) {
        var player = QuarryFakePlayer.get(level);
        QuarryFakePlayerCommon.setDirection(player, Direction.DOWN);
        return player;
    }

    @Override
    public AABB getRenderBoundingBox() {
        var area = getArea();
        if (area == null) {
            return super.getRenderBoundingBox();
        }
        return switch (renderMode()) {
            case "drill" -> new AABB(area.minX(), area.minY(), area.minZ(), area.maxX(), area.maxY(), area.maxZ());
            case null, default -> super.getRenderBoundingBox();
        };
    }
}
