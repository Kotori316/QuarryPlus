package com.yogpc.qp.machines.advpump;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.function.Predicate;

class Target implements Iterator<BlockPos> {
    private final List<BlockPos> posList;
    private ListIterator<BlockPos> iterator;
    private final Predicate<BlockPos> inRange;

    Target(List<BlockPos> posList, Predicate<BlockPos> inRange) {
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

    boolean checkAllFluidsRemoved(Level world, BlockPos center) {
        var stillFluid = posList.stream().<BlockPos>mapMulti((pos, consumer) -> {
                consumer.accept(pos);
                consumer.accept(pos.above());
            })
            .filter(p -> !world.getFluidState(p).isEmpty())
            .filter(inRange)
            .distinct()
            .sorted(Comparator.comparingInt(Vec3i::getY).reversed()
                .thenComparing(Comparator.comparingInt(center::distManhattan).reversed()))
            .toList();
        if (stillFluid.isEmpty()) {
            return false;
        } else {
            this.iterator = stillFluid.listIterator();
            return true;
        }
    }

    static Target getTarget(Level world, BlockPos initPos, Predicate<BlockPos> inRange, Predicate<BlockState> isReplaceBlock, int sizeHint) {
        var result = search(world, Set.of(initPos), inRange, isReplaceBlock, sizeHint);
        result.sort(Comparator.comparingInt(Vec3i::getY).reversed()
            .thenComparing(Comparator.comparingInt(initPos::distManhattan).reversed()));
        return new Target(result, inRange);
    }

    private static List<BlockPos> search(Level world, Set<BlockPos> initialPoses, Predicate<BlockPos> inRange, Predicate<BlockState> isReplaceBlock, int sizeHint) {
        Set<BlockPos> counted = new HashSet<>(sizeHint);
        Set<BlockPos> checked = new HashSet<>(sizeHint);
        List<BlockPos> result = new ArrayList<>(sizeHint);
        Set<Direction> directions = EnumSet.of(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.UP);
        Set<BlockPos> search = initialPoses;
        while (!search.isEmpty()) {
            Set<BlockPos> nextSearch = new HashSet<>(sizeHint);
            checked.addAll(search);
            for (BlockPos pos : search) {
                var isFluid = !world.getFluidState(pos).isEmpty();
                if (isFluid || isReplaceBlock.test(world.getBlockState(pos))) {
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
}
