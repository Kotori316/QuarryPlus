package com.yogpc.qp.machines.quarry;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.Area;
import com.yogpc.qp.machines.MachineStorage;
import com.yogpc.qp.machines.PowerTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

public enum QuarryState implements BlockEntityTicker<TileQuarry> {
    FINISHED(false) {
        @Override
        public void tick(Level world, BlockPos quarryPos, BlockState state, TileQuarry quarry) {
            // Nothing to do.
        }
    },
    WAITING(false) {
        @Override
        public void tick(Level world, BlockPos quarryPos, BlockState state, TileQuarry quarry) {
            if (quarry.getArea() != null && quarry.getEnergy() > quarry.getMaxEnergy() / 200) {
                quarry.setState(BREAK_INSIDE_FRAME, state);
            }
        }
    },
    BREAK_INSIDE_FRAME(true) {
        @Override
        public void tick(Level world, BlockPos quarryPos, BlockState state, TileQuarry quarry) {
            if (world.getGameTime() % headInterval(quarry) != 0) return;
            Objects.requireNonNull(quarry.getArea());
            if (quarry.target == null) {
                // Initial
                quarry.target = Target.newFrameInside(quarry.getArea(), quarry.getArea().minY(), quarry.getArea().maxY());
                QuarryPlus.LOGGER.debug(MARKER, "Quarry({}) Target changed to {} in {}.", quarryPos, quarry.target, name());
            }
            var targetPos = QuarryState.dropUntilPos(quarry.target, StateConditions.skipNoBreak(quarry));
            if (targetPos == null) {
                quarry.target = null;
                quarry.setState(MAKE_FRAME, state);
                return;
            }
            if (!quarry.getTargetWorld().getFluidState(targetPos).isEmpty()) {
                if (quarry.quarryConfig.removeFluid()) {
                    quarry.setState(REMOVE_FLUID, state);
                } else {
                    // Ignore this pos
                    quarry.target.get(true);
                }
            } else if (quarry.breakBlock(targetPos).isSuccess()) {
                quarry.target.get(true); // Set next pos.
            }
        }
    },
    MAKE_FRAME(true) {
        @Override
        public void tick(Level world, BlockPos quarryPos, BlockState state, TileQuarry quarry) {
            Objects.requireNonNull(quarry.getArea());
            if (quarry.target == null) {
                quarry.target = Target.newFrameTarget(quarry.getArea());
                QuarryPlus.LOGGER.debug(MARKER, "Quarry({}) Target changed to {} in {}.", quarryPos, quarry.target, name());
            }
            var targetPos = QuarryState.dropUntilPos(quarry.target, StateConditions.skipFramePlace(quarry));
            if (targetPos == null) {
                quarry.setState(MOVE_HEAD, state);
            } else {
                var breakResult = quarry.breakBlock(targetPos);
                if (breakResult.isSuccess()) {
                    if (quarry.useEnergy(PowerTile.Constants.getMakeFrameEnergy(quarry), PowerTile.Reason.MAKE_FRAME, false)) {
                        quarry.getTargetWorld().setBlockAndUpdate(targetPos, QuarryPlus.ModObjects.BLOCK_FRAME.defaultBlockState());
                    }
                }
            }
        }
    },
    MOVE_HEAD(true) {
        @Override
        public void tick(Level world, BlockPos quarryPos, BlockState state, TileQuarry quarry) {
            Objects.requireNonNull(quarry.getArea());
            if (quarry.target == null) {
                quarry.target = Target.newDigTarget(quarry.getArea(), quarry.getArea().minY());
                QuarryPlus.LOGGER.debug(MARKER, "Quarry({}) Target changed to {} in {}.", quarryPos, quarry.target, name());
            }
            var blockTarget = QuarryState.dropUntilPos(quarry.target, StateConditions.skipNoBreak(quarry));
            if (blockTarget == null) {
                var fluidPoses = quarry.target.allPoses()
                    .filter(p -> !quarry.getTargetWorld().getFluidState(p).isEmpty()).map(BlockPos::immutable).toList();
                if (!quarry.quarryConfig.removeFluid() || fluidPoses.isEmpty()) {
                    // Change Y
                    quarry.target = Target.nextY(quarry.target, quarry.getArea(), quarry.digMinY);
                    QuarryPlus.LOGGER.debug(MARKER, "Quarry({}) Target changed to {} in {}.", quarryPos, quarry.target, name());
                    if (quarry.target != null)
                        tick(world, quarryPos, state, quarry);
                    else
                        quarry.setState(FINISHED, state);
                } else {
                    quarry.target = Target.poses(fluidPoses);
                    quarry.setState(REMOVE_FLUID, state);
                }
            } else {
                var difference = new Vec3(blockTarget.getX() - quarry.headX,
                    blockTarget.getY() - quarry.headY, blockTarget.getZ() - quarry.headZ);
                var squaredDistance = difference.lengthSqr();
                if (squaredDistance > 1e-8) {
                    var moveDistance = Math.min(squaredDistance, quarry.headSpeed());
                    var required = PowerTile.Constants.getMoveEnergy(Math.sqrt(moveDistance), quarry);
                    if (!quarry.useEnergy(required, PowerTile.Reason.MOVE_HEAD, false)) {
                        return;
                    }
                    var normalized = difference.normalize();
                    quarry.headX += normalized.x() * Math.sqrt(moveDistance);
                    quarry.headY += normalized.y() * Math.sqrt(moveDistance);
                    quarry.headZ += normalized.z() * Math.sqrt(moveDistance);
                    quarry.sync();
                }
                if (blockTarget.distToLowCornerSqr(quarry.headX, quarry.headY, quarry.headZ) <= 1e-8)
                    BREAK_BLOCK.tick(world, quarryPos, state, quarry);
            }
        }
    },
    BREAK_BLOCK(true) {
        @Override
        public void tick(Level world, BlockPos quarryPos, BlockState state, TileQuarry quarry) {
            Objects.requireNonNull(quarry.getArea());
            if (quarry.target == null) {
                quarry.target = Target.newDigTarget(quarry.getArea(), quarry.getArea().minY());
                QuarryPlus.LOGGER.debug(MARKER, "Quarry({}) Target changed to {} in {}.", quarryPos, quarry.target, name());
            }
            if (!quarry.getTargetWorld().getFluidState(Objects.requireNonNull(quarry.target.get(false))).isEmpty()) {
                if (quarry.quarryConfig.removeFluid()) {
                    quarry.setState(REMOVE_FLUID, state);
                } else {
                    // Ignore this pos
                    quarry.target.get(true);
                }
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
        public void tick(Level world, BlockPos quarryPos, BlockState state, TileQuarry quarry) {
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
                    if (blockState.getBlock() instanceof LiquidBlock) {
                        var fluidState = targetWorld.getFluidState(fluidPos);
                        if (!fluidState.isEmpty() && fluidState.isSource())
                            quarry.storage.addFluid(fluidState.getType(), MachineStorage.ONE_BUCKET);
                        targetWorld.setBlock(fluidPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                    } else if (blockState.getBlock() instanceof BucketPickup drain) {
                        var bucket = drain.pickupBlock(null, targetWorld, fluidPos, blockState);
                        quarry.storage.addFluid(bucket);
                    } else {
                        // What ?
                        targetWorld.setBlock(fluidPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
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
    public abstract void tick(Level world, BlockPos quarryPos, BlockState state, TileQuarry quarry);

    @Nullable
    private static BlockPos dropUntilPos(Target target, Predicate<BlockPos> condition) {
        var pos = target.get(false);
        while (pos != null && condition.test(pos)) {
            pos = target.goNextAndGet();
        }
        return pos;
    }

    private static Set<BlockPos> countFluid(Level world, BlockPos originalPos, Area area) {
        Set<BlockPos> counted = new HashSet<>();
        Set<Direction> directions = EnumSet.of(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.UP);
        Set<BlockPos> search = Set.of(originalPos);
        Set<BlockPos> checked = new HashSet<>(area.sizeOfEachY());
        while (!search.isEmpty()) {
            Set<BlockPos> nextSearch = new HashSet<>();
            for (BlockPos pos : search) {
                checked.add(pos);
                if (!world.getFluidState(pos).isEmpty()) {
                    if (counted.add(pos)) {
                        directions.stream()
                            .map(pos::relative)
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

    static int headInterval(TileQuarry quarry) {
        return (int) Math.ceil(1 / quarry.headSpeed());
    }
}

class StateConditions {
    static Predicate<BlockPos> skipFramePlace(TileQuarry quarry) {
        var world = quarry.getTargetWorld();
        assert world != null; // This must be called in tick update.
        return pos -> {
            var state = world.getBlockState(pos);
            return state.is(QuarryPlus.ModObjects.BLOCK_FRAME) // Frame
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
