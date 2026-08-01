package com.yogpc.qp.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class FluidDrain {
    private FluidDrain() {
    }

    /**
     * Drains a fluid source block at {@code pos}, replacing it with {@code newState}, and (unless {@code discard})
     * stores the resulting fluid into {@code storage} -- 1 bucket for plain {@link LiquidBlock}s, or whatever a
     * {@link BucketPickup} block yields for modded fluid containers.
     * <p>
     * When {@code state.getBlock()} is a non-{@link LiquidBlock} {@link BucketPickup} implementor, {@code pickupBlock}
     * decides the block's post-removal state itself, so {@code newState} is only applied in the {@link LiquidBlock}
     * and "neither" branches.
     */
    public static void drainSourceInto(Level level, BlockPos pos, @Nullable ServerPlayer player, BlockState newState, MachineStorage storage, boolean discard) {
        var state = level.getBlockState(pos);
        if (state.getBlock() instanceof LiquidBlock) {
            var fluid = level.getFluidState(pos);
            if (!discard && !fluid.isEmpty() && fluid.isSource()) {
                storage.addFluid(fluid.getType(), MachineStorage.ONE_BUCKET);
            }
            level.setBlock(pos, newState, Block.UPDATE_ALL);
        } else if (state.getBlock() instanceof BucketPickup bucketPickup) {
            var picked = bucketPickup.pickupBlock(player, level, pos, state);
            if (!discard) {
                storage.addBucketFluid(picked);
            }
        } else {
            level.setBlock(pos, newState, Block.UPDATE_ALL);
        }
    }
}
