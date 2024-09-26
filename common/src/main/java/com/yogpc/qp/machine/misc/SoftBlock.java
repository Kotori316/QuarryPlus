package com.yogpc.qp.machine.misc;

import com.mojang.serialization.MapCodec;
import com.yogpc.qp.InCreativeTabs;
import com.yogpc.qp.QuarryPlus;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.TickTask;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.function.Predicate;

public final class SoftBlock extends TransparentBlock implements InCreativeTabs {
    public static final String NAME = "soft_block";
    private static final int CHAIN_MAX = Short.MAX_VALUE / 2;
    public final ResourceLocation location = ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, NAME);
    public final BlockItem blockItem;

    public SoftBlock() {
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

    private static final MapCodec<SoftBlock> CODEC = simpleCodec(p -> new SoftBlock());

    @Override
    protected MapCodec<SoftBlock> codec() {
        return CODEC;
    }

    private boolean breaking = false;

    @Override
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
            for (var dir : Direction26.DIRECTIONS) {
                var nPos = first.offset(dir.vec());
                var nBlock = world.getBlockState(nPos);
                if (nBlock.getBlock() == this) {
                    nextCheck.add(nPos);
                }
            }
            if (!nextCheck.isEmpty()) {
                var server = Objects.requireNonNull(world.getServer());
                var tickOffset = world.getRandom().nextIntBetweenInclusive(8, 30);
                server.tell(new TickTask(server.getTickCount() + tickOffset, new ChainBreakTask(world, nextCheck, 1, b -> this.breaking = b, new HashSet<>(), Predicate.isEqual(this))));
            }
        }
    }

    private record ChainBreakTask(
        Level level,
        Collection<BlockPos> targets,
        int totalRemoved,
        BooleanConsumer consumer,
        HashSet<BlockPos> checked,
        Predicate<Block> continueChain
    ) implements Runnable {

        @Override
        public void run() {
            var removed = new HashSet<BlockPos>();
            var nextCheck = new ArrayList<>(targets);
            while (!nextCheck.isEmpty() && removed.size() < CHAIN_MAX) {
                var copied = nextCheck.toArray(new BlockPos[0]);
                nextCheck.clear();
                for (var pos : copied) {
                    for (var dir : Direction26.DIRECTIONS) {
                        var nPos = pos.offset(dir.vec());
                        if (!checked.add(nPos)) {
                            // Skip checked block
                            continue;
                        }
                        var nBlock = level.getBlockState(nPos);
                        if (continueChain.test(nBlock.getBlock())) {
                            if (removed.add(nPos)) {
                                nextCheck.add(nPos);
                            }
                        }
                    }
                    if (checked.add(pos)) {
                        var nBlock = level.getBlockState(pos);
                        if (continueChain.test(nBlock.getBlock())) {
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
                var tickOffset = level.getRandom().nextIntBetweenInclusive(8, 30);
                server.tell(new TickTask(server.getTickCount() + tickOffset, new ChainBreakTask(level, nextCheck, totalRemoved + removed.size(), consumer, checked, continueChain)));
            }
        }
    }
}
