package com.yogpc.qp.machines.mini_quarry;

import java.util.Iterator;

import com.yogpc.qp.machines.Area;
import com.yogpc.qp.machines.TargetIterator;
import net.minecraft.core.BlockPos;

final class MiniTarget implements Iterator<BlockPos> {
    private final Area area;
    private final TargetIterator targetIterator;
    private int y;

    MiniTarget(Area area) {
        this.area = area;
        this.targetIterator = TargetIterator.of(area);
        this.y = area.maxY();
    }

    @Override
    public boolean hasNext() {
        if (this.y > area.minY()) return true;
        else return targetIterator.hasNext(); // The last y.
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
            y -= 1;
            return next();
        }
    }

    void setCurrent(BlockPos pos) {
        this.y = pos.getY();
        this.targetIterator.setCurrent(new TargetIterator.XZPair(pos.getX(), pos.getZ()));
    }
}
