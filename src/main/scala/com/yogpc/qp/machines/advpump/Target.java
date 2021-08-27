package com.yogpc.qp.machines.advpump;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.function.Predicate;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

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

    public Predicate<BlockPos> getPredicate() {
        return inRange;
    }

    public boolean checkAllFluidsRemoved(World world, BlockPos center) {
        var stillFluid = search(world, Set.copyOf(posList), inRange);
        if (stillFluid.isEmpty()) {
            return false;
        } else {
            stillFluid.sort(Comparator.comparingInt(BlockPos::getY).reversed()
                .thenComparing(Comparator.comparingInt(center::getManhattanDistance).reversed()));
            this.iterator = stillFluid.listIterator();
            return true;
        }
    }

    static Target getTarget(World world, BlockPos initPos, Predicate<BlockPos> inRange) {
        var result = search(world, Set.of(initPos), inRange);
        result.sort(Comparator.comparingInt(BlockPos::getY).reversed()
            .thenComparing(Comparator.comparingInt(initPos::getManhattanDistance).reversed()));
        return new Target(result, inRange);
    }

    private static List<BlockPos> search(World world, Set<BlockPos> initialPoses, Predicate<BlockPos> inRange) {
        Set<BlockPos> counted = new HashSet<>();
        Set<BlockPos> checked = new HashSet<>();
        List<BlockPos> result = new ArrayList<>();
        Set<Direction> directions = EnumSet.of(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.UP);
        Set<BlockPos> search = initialPoses;
        while (!search.isEmpty()) {
            Set<BlockPos> nextSearch = new HashSet<>();
            checked.addAll(search);
            for (BlockPos pos : search) {
                if (!world.getFluidState(pos).isEmpty()) {
                    if (counted.add(pos)) {
                        result.add(pos);
                        directions.stream()
                            .map(pos::offset)
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
