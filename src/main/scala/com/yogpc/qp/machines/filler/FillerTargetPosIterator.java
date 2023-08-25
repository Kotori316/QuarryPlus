package com.yogpc.qp.machines.filler;

import com.yogpc.qp.machines.Area;
import com.yogpc.qp.machines.PickIterator;
import com.yogpc.qp.machines.TargetIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import scala.collection.immutable.Seq;

@SuppressWarnings("DuplicatedCode")
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

    abstract FillerEntity.Action type();

    public static FillerTargetPosIterator box(Area area) {
        return new Box(area);
    }

    static final class Box extends FillerTargetPosIterator {
        TargetIterator iterator;

        Box(Area area) {
            super(area);
            setNewIterator();
            reset();
        }

        @Override
        FillerEntity.Action type() {
            return FillerEntity.Action.BOX;
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
        FillerEntity.Action type() {
            return FillerEntity.Action.WALL;
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

    static final class Pillar extends FillerTargetPosIterator {
        private scala.collection.Iterator<TargetIterator.XZPair> iterator;

        Pillar(Area area) {
            super(area);
            setNewIterator();
            reset();
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

        private Seq<TargetIterator.XZPair> getCircle() {
            return CircleGenerator.makeCircle(
                new TargetIterator.XZPair((area.minX() + area.maxX() + 1) / 2, (area.minZ() + area.maxZ() + 1) / 2),
                Math.min(area.maxX() - area.minX() + 1, area.maxZ() - area.minZ() + 1)
            );
        }

        @Override
        public BlockPos head() {
            var firstXZ = getCircle().head();
            return new BlockPos(firstXZ.x(), minY, firstXZ.z());
        }

        @Override
        public void reset() {
            this.iterator.next(); // Go to the next because we already get the first element.
            super.reset();
        }

        @Override
        public void setCurrent(BlockPos current) {
            super.setCurrent(current);
            var t = new TargetIterator.XZPair(current.getX(), current.getZ());
            this.iterator = this.iterator.dropWhile(p -> !p.equals(t)).drop(1);
        }

        private void setNewIterator() {
            this.iterator = getCircle().iterator();
        }

        @Override
        FillerEntity.Action type() {
            return FillerEntity.Action.PILLAR;
        }
    }
}
