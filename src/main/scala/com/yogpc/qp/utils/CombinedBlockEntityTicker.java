package com.yogpc.qp.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;

public record CombinedBlockEntityTicker<T extends BlockEntity>(
    List<BlockEntityTicker<? super T>> tickers) implements BlockEntityTicker<T> {
    @SafeVarargs
    public CombinedBlockEntityTicker(BlockEntityTicker<? super T>... ts) {
        this(Arrays.stream(ts).filter(Objects::nonNull).toList());
    }

    @Override
    public void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        for (BlockEntityTicker<? super T> ticker : tickers) {
            ticker.tick(level, pos, state, blockEntity);
        }
    }
}
