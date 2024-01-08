package com.yogpc.qp.machines.misc;

import com.mojang.serialization.MapCodec;
import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.Direction8;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.TickTask;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

public class BlockDummy extends TransparentBlock {
    public static final String NAME = "dummy";
    public final ResourceLocation location = new ResourceLocation(QuarryPlus.modID, NAME);
    public final BlockItem blockItem;

    public BlockDummy() {
        super(Properties.of()
            .mapColor(MapColor.NONE)
            .noOcclusion()
            .noLootTable()
            .isValidSpawn((state, world, pos, type) -> false)
            .isSuffocating((state, world, pos) -> false)
            .isRedstoneConductor((state, world, pos) -> false)
            .isViewBlocking((state, world, pos) -> false)
        );
        blockItem = new BlockItem(this, new Item.Properties());
    }

    private static final MapCodec<BlockDummy> CODEC = simpleCodec(p -> new BlockDummy());

    @Override
    protected MapCodec<BlockDummy> codec() {
        return CODEC;
    }

    private boolean breaking = false;

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            if (!breaking) {
                breaking = true;
                breakChain(world, pos);
                breaking = false;
            }
            super.onRemove(state, world, pos, newState, moved);
        }
    }

    private void breakChain(Level world, BlockPos first) {
        if (!world.isClientSide) {
            var nextCheck = new ArrayList<BlockPos>();
            for (var dir : Direction8.DIRECTIONS) {
                var nPos = first.offset(dir.vec());
                var nBlock = world.getBlockState(nPos);
                if (nBlock.getBlock() == this) {
                    nextCheck.add(nPos);
                }
            }
            if (!nextCheck.isEmpty()) {
                var server = Objects.requireNonNull(world.getServer());
                server.tell(new TickTask(server.getTickCount() + 4, new ChainBreakTask(world, nextCheck, 1, b -> this.breaking = b, new HashSet<>())));
            }
        }
    }

    private static class ChainBreakTask implements Runnable {
        private final Level level;
        private final Collection<BlockPos> targets;
        private final int totalRemoved;
        private final BooleanConsumer consumer;
        private final HashSet<BlockPos> checked;

        ChainBreakTask(Level level, Collection<BlockPos> targets, int totalRemoved, BooleanConsumer consumer, HashSet<BlockPos> checked) {
            this.level = level;
            this.targets = targets;
            this.totalRemoved = totalRemoved;
            this.consumer = consumer;
            this.checked = checked;
        }

        @Override
        public void run() {
            var removed = new HashSet<BlockPos>();
            var nextCheck = new ArrayList<>(targets);
            while (!nextCheck.isEmpty() && removed.size() < Short.MAX_VALUE / 2) {
                var copied = nextCheck.toArray(new BlockPos[0]);
                nextCheck.clear();
                for (var pos : copied) {
                    for (var dir : Direction8.DIRECTIONS) {
                        var nPos = pos.offset(dir.vec());
                        if (!checked.add(nPos)) {
                            // Skip checked block
                            continue;
                        }
                        var nBlock = level.getBlockState(nPos);
                        if (nBlock.getBlock() == Holder.BLOCK_DUMMY) {
                            if (removed.add(nPos)) {
                                nextCheck.add(nPos);
                            }
                        }
                    }
                    if (checked.add(pos)) {
                        var nBlock = level.getBlockState(pos);
                        if (nBlock.getBlock() == Holder.BLOCK_DUMMY) {
                            removed.add(pos);
                        }
                    }
                }
            }

            consumer.accept(true);
            removed.forEach(p -> level.removeBlock(p, false));
            consumer.accept(false);

            if (!nextCheck.isEmpty()) {
                var server = Objects.requireNonNull(level.getServer());
                server.tell(new TickTask(server.getTickCount() + 4, new ChainBreakTask(level, nextCheck, totalRemoved + removed.size(), consumer, checked)));
            }
        }
    }
}
