package com.yogpc.qp.utils;

import java.util.Arrays;
import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final record CombinedBlockEntityTicker<T extends BlockEntity>(
    List<BlockEntityTicker<? super T>> tickers) implements BlockEntityTicker<T> {
    @SafeVarargs
    public CombinedBlockEntityTicker(BlockEntityTicker<? super T>... ts) {
        this(Arrays.asList(ts));
    }

    @Override
    public void tick(World world, BlockPos pos, BlockState state, T blockEntity) {
        for (BlockEntityTicker<? super T> ticker : tickers) {
            ticker.tick(world, pos, state, blockEntity);
        }
    }
}
