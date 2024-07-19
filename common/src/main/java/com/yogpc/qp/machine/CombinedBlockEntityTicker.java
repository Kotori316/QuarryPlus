package com.yogpc.qp.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public record CombinedBlockEntityTicker<T extends BlockEntity>(
    List<BlockEntityTicker<? super T>> tickers) implements BlockEntityTicker<T> {
    @SafeVarargs
    @Nullable
    public static <T extends BlockEntity> CombinedBlockEntityTicker<T> of(QpBlock block, Level level, BlockEntityTicker<? super T>... ts) {
        return new CombinedBlockEntityTicker<>(Arrays.stream(ts).filter(Objects::nonNull).toList());
    }

    @Override
    public void tick(Level world, BlockPos pos, BlockState state, T blockEntity) {
        for (BlockEntityTicker<? super T> ticker : tickers) {
            ticker.tick(world, pos, state, blockEntity);
        }
    }
}
