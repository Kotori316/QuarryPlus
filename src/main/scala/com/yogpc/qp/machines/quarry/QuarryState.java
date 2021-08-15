package com.yogpc.qp.machines.quarry;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.Area;
import com.yogpc.qp.machines.MachineStorage;
import com.yogpc.qp.machines.PowerTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.FluidDrainable;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.Nullable;

public enum QuarryState implements BlockEntityTicker<TileQuarry> {
    FINISHED(false) {
        @Override
        public void tick(World world, BlockPos quarryPos, BlockState state, TileQuarry quarry) {
            // Nothing to do.
        }
    },
    WAITING(false) {
        @Override
        public void tick(World world, BlockPos quarryPos, BlockState state, TileQuarry quarry) {
            if (quarry.getArea() != null && quarry.getEnergy() > quarry.getMaxEnergy() / 200) {
                quarry.setState(MAKE_FRAME, state);
            }
        }
    },
    MAKE_FRAME(true) {
        @Override
        public void tick(World world, BlockPos quarryPos, BlockState state, TileQuarry quarry) {
            Objects.requireNonNull(quarry.getArea());
            if (quarry.target == null) {
                quarry.target = Target.newFrameTarget(quarry.getArea());
                QuarryPlus.LOGGER.debug(MARKER, "Quarry({}) Target changed to {}.", quarryPos, quarry.target);
            }
            var targetPos = QuarryState.dropUntilPos(quarry.target, StateConditions.skipFramePlace(quarry));
            if (targetPos == null) {
                quarry.setState(MOVE_HEAD, state);
            } else {
                var breakResult = quarry.breakBlock(targetPos);
                if (breakResult.isSuccess()) {
                    if (quarry.useEnergy(PowerTile.Constants.getMakeFrameEnergy(quarry), PowerTile.Reason.MAKE_FRAME, false)) {
                        quarry.getTargetWorld().setBlockState(targetPos, QuarryPlus.ModObjects.BLOCK_FRAME.getDefaultState());
                    }
                }
            }
        }
    },
    MOVE_HEAD(true) {
        @Override
        public void tick(World world, BlockPos quarryPos, BlockState state, TileQuarry quarry) {
            Objects.requireNonNull(quarry.getArea());
            if (quarry.target == null) {
                quarry.target = Target.newDigTarget(quarry.getArea(), quarry.getArea().minY());
                QuarryPlus.LOGGER.debug(MARKER, "Quarry({}) Target changed to {}.", quarryPos, quarry.target);
            }
            var blockTarget = QuarryState.dropUntilPos(quarry.target, StateConditions.skipNoBreak(quarry));
            if (blockTarget == null) {
                var fluidPoses = quarry.target.allPoses()
                    .filter(p -> !quarry.getTargetWorld().getFluidState(p).isEmpty()).map(BlockPos::toImmutable).toList();
                if (fluidPoses.isEmpty()) {
                    // Change Y
                    quarry.target = Target.nextY(quarry.target, quarry.getArea(), quarry.digMinY);
                    QuarryPlus.LOGGER.debug(MARKER, "Quarry({}) Target changed to {}.", quarryPos, quarry.target);
                    if (quarry.target != null)
                        tick(world, quarryPos, state, quarry);
                    else
                        quarry.setState(FINISHED, state);
                } else {
                    quarry.target = Target.poses(fluidPoses);
                    quarry.setState(REMOVE_FLUID, state);
                }
            } else {
                var difference = new Vec3d(blockTarget.getX() - quarry.headX,
                    blockTarget.getY() - quarry.headY, blockTarget.getZ() - quarry.headZ);
                var squaredDistance = difference.lengthSquared();
                if (squaredDistance > 1e-8) {
                    var moveDistance = Math.min(squaredDistance, quarry.headSpeed());
                    var required = PowerTile.Constants.getMoveEnergy(Math.sqrt(moveDistance), quarry);
                    if (!quarry.useEnergy(required, PowerTile.Reason.MOVE_HEAD, false)) {
                        return;
                    }
                    var normalized = difference.normalize();
                    quarry.headX += normalized.getX() * Math.sqrt(moveDistance);
                    quarry.headY += normalized.getY() * Math.sqrt(moveDistance);
                    quarry.headZ += normalized.getZ() * Math.sqrt(moveDistance);
                    quarry.sync();
                }
                if (blockTarget.getSquaredDistance(quarry.headX, quarry.headY, quarry.headZ, false) <= 1e-8)
                    BREAK_BLOCK.tick(world, quarryPos, state, quarry);
            }
        }
    },
    BREAK_BLOCK(true) {
        @Override
        public void tick(World world, BlockPos quarryPos, BlockState state, TileQuarry quarry) {
            Objects.requireNonNull(quarry.getArea());
            if (quarry.target == null) {
                quarry.target = Target.newDigTarget(quarry.getArea(), quarry.getArea().minY());
                QuarryPlus.LOGGER.debug(MARKER, "Quarry({}) Target changed to {} in BREAK_BLOCK.", quarryPos, quarry.target);
            }
            if (!quarry.getTargetWorld().getFluidState(Objects.requireNonNull(quarry.target.get(false))).isEmpty()) {
                quarry.setState(REMOVE_FLUID, state);
            } else if (quarry.breakBlock(Objects.requireNonNull(quarry.target.get(false))).isSuccess()) {
                quarry.target.get(true); // Set next pos.
                quarry.setState(MOVE_HEAD, state);
            } else {
                quarry.setState(BREAK_BLOCK, state);
            }
        }
    },
    REMOVE_FLUID(true) {
        @Override
        public void tick(World world, BlockPos quarryPos, BlockState state, TileQuarry quarry) {
            Objects.requireNonNull(quarry.getArea());
            if (quarry.target == null) {
                quarry.target = Target.newDigTarget(quarry.getArea(), quarry.getArea().minY());
                QuarryPlus.LOGGER.debug(MARKER, "Quarry({}) Target changed to {} in {}.", quarryPos, quarry.target, name());
            }
            var original = Objects.requireNonNull(quarry.target.get(false));
            var targetWorld = quarry.getTargetWorld();
            Set<BlockPos> fluidPoses = countFluid(targetWorld, original, quarry.getArea());
            if (quarry.useEnergy(PowerTile.Constants.getBreakBlockFluidEnergy(quarry) * fluidPoses.size(), PowerTile.Reason.REMOVE_FLUID, true)) {
                for (BlockPos fluidPos : fluidPoses) {
                    var blockState = targetWorld.getBlockState(fluidPos);
                    if (blockState.getBlock() instanceof FluidBlock) {
                        var fluidState = targetWorld.getFluidState(fluidPos);
                        if (!fluidState.isEmpty() && fluidState.isStill())
                            quarry.storage.addFluid(fluidState.getFluid(), MachineStorage.ONE_BUCKET);
                        targetWorld.setBlockState(fluidPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
                    } else if (blockState.getBlock() instanceof FluidDrainable drain) {
                        var bucket = drain.tryDrainFluid(targetWorld, fluidPos, blockState);
                        quarry.storage.addFluid(bucket);
                    } else {
                        // What ?
                        targetWorld.setBlockState(fluidPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
                    }
                    TileQuarry.checkEdgeFluid(fluidPos, false, targetWorld, quarry);
                }
                quarry.setState(BREAK_BLOCK, state);
            }
        }
    };
    public final boolean isWorking;
    private static final Marker MARKER = MarkerManager.getMarker("QuarryState");

    QuarryState(boolean isWorking) {
        this.isWorking = isWorking;
    }

    @Override
    public abstract void tick(World world, BlockPos quarryPos, BlockState state, TileQuarry quarry);

    @Nullable
    private static BlockPos dropUntilPos(Target target, Predicate<BlockPos> condition) {
        var pos = target.get(false);
        while (pos != null && condition.test(pos)) {
            pos = target.goNextAndGet();
        }
        return pos;
    }

    private static Set<BlockPos> countFluid(World world, BlockPos originalPos, Area area) {
        Set<BlockPos> counted = new HashSet<>();
        Set<Direction> directions = EnumSet.of(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.UP);
        Set<BlockPos> search = Set.of(originalPos);
        Set<BlockPos> checked = new HashSet<>();
        while (!search.isEmpty()) {
            Set<BlockPos> nextSearch = new HashSet<>();
            for (BlockPos pos : search) {
                checked.add(pos);
                if (!world.getFluidState(pos).isEmpty()) {
                    if (counted.add(pos)) {
                        directions.stream()
                            .map(pos::offset)
                            .filter(area::isInAreaIgnoreY)
                            .filter(Predicate.not(checked::contains))
                            .forEach(nextSearch::add);
                    }
                }
            }
            search = nextSearch;
        }
        return counted;
    }
}

class StateConditions {
    static Predicate<BlockPos> skipFramePlace(TileQuarry quarry) {
        var world = quarry.getTargetWorld();
        assert world != null; // This must be called in tick update.
        return pos -> {
            var state = world.getBlockState(pos);
            return state.isOf(QuarryPlus.ModObjects.BLOCK_FRAME) // Frame
                || !quarry.canBreak(world, pos, state) // Unbreakable
                ;
        };
    }

    static Predicate<BlockPos> skipNoBreak(TileQuarry quarry) {
        var world = quarry.getTargetWorld();
        assert world != null; // This must be called in tick update.
        return pos -> {
            var state = world.getBlockState(pos);
            return state.isAir() // Air
                || !quarry.canBreak(world, pos, state) // Unbreakable
                ;
        };
    }
}
