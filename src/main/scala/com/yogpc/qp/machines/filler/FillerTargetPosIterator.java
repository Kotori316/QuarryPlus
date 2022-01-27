package com.yogpc.qp.machines.filler;

import com.yogpc.qp.machines.Area;
import com.yogpc.qp.machines.PickIterator;
import com.yogpc.qp.machines.TargetIterator;
import net.minecraft.core.BlockPos;

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
}
