package com.yogpc.qp.machines.mini_quarry;

import com.yogpc.qp.machines.Area;
import com.yogpc.qp.machines.TargetIterator;
import net.minecraft.core.BlockPos;

import java.util.Iterator;

abstract class MiniTarget implements Iterator<BlockPos> {
    protected final Area area;
    protected final TargetIterator targetIterator;
    protected int y;

    MiniTarget(Area area) {
        this.area = area;
        this.targetIterator = TargetIterator.of(area);
        this.y = area.maxY();
    }

    static MiniTarget of(Area area, boolean repeat) {
        if (repeat) {
            return new Repeat(area);
        } else {
            return new Single(area);
        }
    }

    public BlockPos peek() {
        var pair = targetIterator.peek();
        return new BlockPos(pair.x(), y, pair.z());
    }

    @Override
    public BlockPos next() {
        if (targetIterator.hasNext()) {
            var current = peek();
            targetIterator.next();
            return current;
        } else {
            targetIterator.reset();
            y = nextY();
            return next();
        }
    }

    protected abstract int nextY();

    void setCurrent(BlockPos pos) {
        this.y = pos.getY();
        this.targetIterator.setCurrent(new TargetIterator.XZPair(pos.getX(), pos.getZ()));
    }

    private static class Single extends MiniTarget {
        Single(Area area) {
            super(area);
        }

        @Override
        public boolean hasNext() {
            if (this.y > area.minY()) return true;
            else return targetIterator.hasNext(); // The last y.
        }

        @Override
        protected int nextY() {
            return y - 1;
        }
    }

    private static class Repeat extends MiniTarget {
        private boolean isRepeating;

        Repeat(Area area) {
            super(area);
        }

        @Override
        public boolean hasNext() {
            if (!isRepeating || this.y > area.minY()) return true;
            else return targetIterator.hasNext(); // The last y.
        }

        @Override
        protected int nextY() {
            if (isRepeating) {
                isRepeating = false;
                return y - 1;
            } else {
                isRepeating = true;
                return y;
            }
        }
    }
}
