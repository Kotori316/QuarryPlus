package com.yogpc.qp.machines.filler;

import com.yogpc.qp.machines.Area;
import com.yogpc.qp.machines.PickIterator;
import com.yogpc.qp.machines.TargetIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public abstract class FillerTargetPosIterator extends PickIterator<BlockPos> {
    final Area area;
    final int maxY;
    final int minY;

    protected FillerTargetPosIterator(Area area) {
        this.area = area;
        this.maxY = area.maxY();
        this.minY = area.minY();
    }

    @Override
    public boolean hasNext() {
        return area.minX() <= current.getX() && current.getX() <= area.maxX() &&
            area.minZ() <= current.getZ() && current.getZ() <= area.maxZ() &&
            this.minY <= current.getY() && current.getY() <= this.maxY;
    }

    static final class Box extends FillerTargetPosIterator {
        TargetIterator iterator;

        Box(Area area) {
            super(area);
            setNewIterator();
            reset();
        }

        @Override
        public void reset() {
            this.iterator.reset();
            this.iterator.next(); // Go to the next because we already get the first element.
            super.reset();
        }

        @Override
        protected BlockPos update() {
            if (this.iterator.hasNext()) {
                var xz = this.iterator.next();
                return new BlockPos(xz.x(), current.getY(), xz.z());
            } else {
                setNewIterator();
                var xz = this.iterator.next();
                return new BlockPos(xz.x(), current.getY() + 1, xz.z());
            }
        }

        @Override
        public void setCurrent(BlockPos current) {
            super.setCurrent(current);
            this.iterator.setCurrent(new TargetIterator.XZPair(current.getX(), current.getZ()));
            this.iterator.next();
        }

        @Override
        public BlockPos head() {
            var head = this.iterator.head();
            return new BlockPos(head.x(), minY, head.z());
        }

        private void setNewIterator() {
            var fixedArea = new Area(area.minX() - 1, area.minY(), area.minZ() - 1, area.maxX() + 1, area.maxY(), area.maxZ() + 1, area.direction());
            iterator = TargetIterator.of(fixedArea);
        }
    }

    static final class Wall extends FillerTargetPosIterator {
        Wall(Area area) {
            super(area);
            reset();
        }

        @Override
        protected BlockPos update() {
            final BlockPos next;
            if (current.getY() == minY || current.getY() == maxY) {
                if (current.getX() < area.maxX()) {
                    // Move x
                    next = current.relative(Direction.Axis.X, 1);
                } else {
                    if (current.getZ() < area.maxZ()) {
                        // Move z and reset x
                        next = new BlockPos(area.minX(), current.getY(), current.getZ() + 1);
                    } else {
                        // Move y and reset x, z
                        next = new BlockPos(area.minX(), current.getY() + 1, area.minZ());
                    }
                }
            } else {
                if (current.getX() == area.minX()) {
                    if (current.getZ() == area.maxZ()) next = current.relative(Direction.Axis.X, 1);
                    else next = current.relative(Direction.Axis.Z, 1);
                } else if (current.getZ() == area.maxZ()) {
                    if (current.getX() == area.maxX()) next = current.relative(Direction.Axis.Z, -1);
                    else next = current.relative(Direction.Axis.X, 1);
                } else if (current.getX() == area.maxX()) {
                    if (current.getZ() == area.minZ()) next = current.relative(Direction.Axis.X, -1);
                    else next = current.relative(Direction.Axis.Z, -1);
                } else if (current.getZ() == area.minZ()) {
                    if (current.getX() == area.minX() + 1) {
                        // Go to next Y
                        next = new BlockPos(area.minX(), current.getY() + 1, area.minZ());
                    } else next = current.relative(Direction.Axis.X, -1);
                } else {
                    // What?
                    throw new IllegalStateException("Bad Position(%s) in %s".formatted(current, area));
                }
            }
            current = next;
            return next;
        }

        @Override
        public BlockPos head() {
            return new BlockPos(area.minX(), minY, area.minZ());
        }
    }
}
