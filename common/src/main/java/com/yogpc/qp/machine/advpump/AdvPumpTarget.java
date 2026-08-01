package com.yogpc.qp.machine.advpump;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.function.Predicate;

/**
 * The BFS work-queue of a single Y layer for {@link AdvPumpEntity}. Positions are found by flooding out from the
 * initial position through fluid blocks and already-processed replacement blocks, then sorted so draining proceeds
 * from the outside/top of the pocket inward.
 */
final class AdvPumpTarget implements Iterator<BlockPos> {
    private final List<BlockPos> posList;
    private ListIterator<BlockPos> iterator;
    private final Predicate<BlockPos> inRange;

    private AdvPumpTarget(List<BlockPos> posList, Predicate<BlockPos> inRange) {
        this.posList = posList;
        this.iterator = posList.listIterator();
        this.inRange = inRange;
    }

    @Override
    public boolean hasNext() {
        return this.iterator.hasNext();
    }

    @Override
    public BlockPos next() {
        return this.iterator.next();
    }

    Predicate<BlockPos> getPredicate() {
        return inRange;
    }

    /**
     * Resumes the iterator over any refilled fluid found near the already-processed positions.
     *
     * @return {@code true} if the iterator is empty; {@code false} if there is still fluids in area.
     */
    boolean updateToRemainingIterator(Level level, BlockPos center) {
        var stillFluid = posList.stream().<BlockPos>mapMulti((pos, consumer) -> {
                consumer.accept(pos);
                consumer.accept(pos.above());
            })
            .filter(p -> !level.getFluidState(p).isEmpty())
            .filter(inRange)
            .distinct()
            .sorted(Comparator.comparingInt(Vec3i::getY).reversed()
                .thenComparing(Comparator.comparingInt(center::distManhattan).reversed()))
            .toList();
        if (stillFluid.isEmpty()) {
            return true;
        } else {
            this.iterator = stillFluid.listIterator();
            return false;
        }
    }

    static AdvPumpTarget getTarget(Level level, BlockPos initPos, Predicate<BlockPos> inRange, Predicate<BlockState> isReplaceBlock, int sizeHint) {
        var result = search(level, Set.of(initPos), inRange, isReplaceBlock, sizeHint);
        result.sort(Comparator.comparingInt(Vec3i::getY).reversed()
            .thenComparing(Comparator.comparingInt(initPos::distManhattan).reversed()));
        return new AdvPumpTarget(result, inRange);
    }

    private static List<BlockPos> search(Level level, Set<BlockPos> initialPoses, Predicate<BlockPos> inRange, Predicate<BlockState> isReplaceBlock, int sizeHint) {
        Set<BlockPos> counted = new HashSet<>(sizeHint);
        Set<BlockPos> checked = new HashSet<>(sizeHint);
        List<BlockPos> result = new ArrayList<>(sizeHint);
        Set<Direction> directions = EnumSet.of(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.UP);
        Set<BlockPos> search = initialPoses;
        while (!search.isEmpty()) {
            Set<BlockPos> nextSearch = new HashSet<>(sizeHint);
            checked.addAll(search);
            for (BlockPos pos : search) {
                var isFluid = !level.getFluidState(pos).isEmpty();
                if (isFluid || isReplaceBlock.test(level.getBlockState(pos))) {
                    if (counted.add(pos)) {
                        if (isFluid) result.add(pos);
                        directions.stream()
                            .map(pos::relative)
                            .filter(inRange)
                            .filter(Predicate.not(checked::contains))
                            .forEach(nextSearch::add);
                    }
                }
            }
            search = nextSearch;
        }
        return result;
    }

    /**
     * Circular horizontal range predicate centered on {@code center}, ignoring Y.
     */
    static Predicate<BlockPos> inRangePredicate(BlockPos center, int range) {
        return p -> {
            var xDiff = center.getX() - p.getX();
            var zDiff = center.getZ() - p.getZ();
            return xDiff * xDiff + zDiff * zDiff < range * range;
        };
    }

    /**
     * Pre-sizing hint for the BFS's internal sets/lists.
     */
    static int areaSizeHint(int range) {
        return (int) (Math.PI * range * range);
    }
}
