package com.yogpc.qp.machines.mini_quarry;

import java.util.Iterator;

import com.yogpc.qp.machines.Area;
import com.yogpc.qp.machines.TargetIterator;
import net.minecraft.core.BlockPos;

final class MiniTarget implements Iterator<BlockPos> {
    private final Area area;
    private TargetIterator targetIterator;

    MiniTarget(Area area) {
        this.area = area;
        this.targetIterator = TargetIterator.of(area);
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public BlockPos next() {
        return null;
    }
}
