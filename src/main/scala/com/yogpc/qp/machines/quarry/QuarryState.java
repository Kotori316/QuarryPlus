package com.yogpc.qp.machines.quarry;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.Area;
import com.yogpc.qp.machines.PowerManager;
import com.yogpc.qp.machines.PowerTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidAttributes;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.Nullable;

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
                LOGGER.debug(MARKER, "{}({}) Target changed to {} in {}.", quarry.getClass().getSimpleName(), quarryPos, quarry.target, name());
            }
            var targetPos = QuarryState.dropUntilPos(quarry.target, StateConditions.skipNoBreak(quarry));
            if (targetPos == null) {
                quarry.target = null;
                quarry.setState(MAKE_FRAME, state);
                return;
            }
            if (TileQuarry.isFullFluidBlock(quarry.getTargetWorld().getBlockState(targetPos))) {
                if (quarry.hasPumpModule())
                    quarry.setState(REMOVE_FLUID, state);
                else
                    quarry.target.get(true); // Set next pos. Ignore the fluid block.
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
                logTargetChange(quarryPos, quarry);
            }
            var targetPos = QuarryState.dropUntilPos(quarry.target, StateConditions.skipFramePlace(quarry));
            if (targetPos == null) {
                quarry.setState(MOVE_HEAD, state);
            } else {
                var breakResult = quarry.breakBlock(targetPos);
                if (breakResult.isSuccess()) {
                    if (quarry.useEnergy(PowerManager.getMakeFrameEnergy(quarry), PowerTile.Reason.MAKE_FRAME, false)) {
                        quarry.getTargetWorld().setBlockAndUpdate(targetPos, Holder.BLOCK_FRAME.defaultBlockState());
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
                logTargetChange(quarryPos, quarry);
            }
            var blockTarget = QuarryState.dropUntilPos(quarry.target, StateConditions.skipNoBreak(quarry));
            if (blockTarget == null) {
                var fluidPoses = quarry.target.allPoses()
                    .filter(p -> TileQuarry.isFullFluidBlock(quarry.getTargetWorld().getBlockState(p))).map(BlockPos::immutable).toList();
                if (!quarry.hasPumpModule() || fluidPoses.isEmpty()) {
                    // Change Y
                    quarry.target = Target.nextY(quarry.target, quarry.getArea(), quarry.digMinY);
                    logTargetChange(quarryPos, quarry);
                    if (quarry.target != null)
                        tick(world, quarryPos, state, quarry);
                    else {
                        // quarry.target == null
                        if (quarry.hasFillerModule()) quarry.setState(FILLER, state);
                        else quarry.setState(FINISHED, state);
                    }
                } else {
                    quarry.target = Target.poses(fluidPoses);
                    quarry.setState(REMOVE_FLUID, state);
                }
            } else {
                var difference = new Vec3(blockTarget.getX() - quarry.headX,
                    blockTarget.getY() - quarry.headY, blockTarget.getZ() - quarry.headZ);
                var distance = difference.length();
                if (distance > 1e-4) {
                    var moveDistance = Math.min(distance, quarry.headSpeed());
                    var required = PowerManager.getMoveEnergy(moveDistance, quarry);
                    if (!quarry.useEnergy(required, PowerTile.Reason.MOVE_HEAD, false)) {
                        return;
                    }
                    var normalized = difference.normalize();
                    quarry.headX += normalized.x() * moveDistance;
                    quarry.headY += normalized.y() * moveDistance;
                    quarry.headZ += normalized.z() * moveDistance;
                    quarry.sync();
                }
                if (blockTarget.distSqr(quarry.headX, quarry.headY, quarry.headZ, false) <= 1e-8)
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
                logTargetChange(quarryPos, quarry);
            }
            var targetPos = Objects.requireNonNull(quarry.target.get(false));
            if (TileQuarry.isFullFluidBlock(quarry.getTargetWorld().getBlockState(targetPos))) {
                if (quarry.hasPumpModule())
                    quarry.setState(REMOVE_FLUID, state);
                else quarry.target.get(true); // Set next pos. Ignore fluid block.
            } else if (quarry.breakBlock(targetPos).isSuccess()) {
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
                logTargetChange(quarryPos, quarry);
            }
            var original = Objects.requireNonNull(quarry.target.get(false));
            var targetWorld = quarry.getTargetWorld();
            Set<BlockPos> fluidPoses = countFluid(targetWorld, original, quarry.getArea());
            if (quarry.useEnergy(PowerManager.getBreakBlockFluidEnergy(quarry) * fluidPoses.size(), PowerTile.Reason.REMOVE_FLUID, true)) {
                for (BlockPos fluidPos : fluidPoses) {
                    var blockState = targetWorld.getBlockState(fluidPos);
                    if (blockState.getBlock() instanceof LiquidBlock) {
                        var fluidState = targetWorld.getFluidState(fluidPos);
                        if (!fluidState.isEmpty() && fluidState.isSource())
                            quarry.storage.addFluid(fluidState.getType(), FluidAttributes.BUCKET_VOLUME);
                        targetWorld.setBlock(fluidPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    } else if (blockState.getBlock() instanceof BucketPickup drain) {
                        var bucket = drain.pickupBlock(targetWorld, fluidPos, blockState);
                        quarry.storage.addFluid(bucket);
                    } else {
                        // What ?
                        targetWorld.setBlock(fluidPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    }
                    TileQuarry.removeEdgeFluid(fluidPos, targetWorld, quarry);
                }
                quarry.setState(BREAK_BLOCK, state);
            }
        }
    },

    FILLER(true) {
        @Override
        public void tick(Level world, BlockPos quarryPos, BlockState state, TileQuarry quarry) {
            Objects.requireNonNull(quarry.getArea());
            if (!(quarry.target instanceof FillerTarget)) {
                var area = quarry.getArea();
                quarry.target = Target.newFillerTarget(new Area(
                    area.minX() + 1, quarry.digMinY + 1, area.minZ() + 1,
                    area.maxX() - 1, area.minY() - 1, area.maxZ() - 1, area.direction()));
                logTargetChange(quarryPos, quarry);
            }
            var action = ((FillerTarget) quarry.target).fillerAction;
            var energy = PowerManager.getFillerEnergy(quarry) * 10;
            action.tick(() -> Optional.of(new ItemStack(Items.STONE)), quarry, energy);
            if (action.isFinished()) {
                quarry.target = null;
                quarry.setState(FINISHED, state);
            }
        }
    };
    public final boolean isWorking;
    private static final Logger LOGGER = QuarryPlus.LOGGER;
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

    void logTargetChange(BlockPos quarryPos, TileQuarry quarry) {
        LOGGER.debug(MARKER, "{}({}) Target changed to {} in {}.", quarry.getClass().getSimpleName(), quarryPos, quarry.target, name());
    }
}

class StateConditions {
    static Predicate<BlockPos> skipFramePlace(TileQuarry quarry) {
        var world = quarry.getTargetWorld();
        assert world != null; // This must be called in tick update.
        return pos -> {
            var state = world.getBlockState(pos);
            return state.is(Holder.BLOCK_FRAME) // Frame
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
