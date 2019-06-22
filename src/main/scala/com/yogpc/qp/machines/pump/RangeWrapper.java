package com.yogpc.qp.machines.pump;

import java.util.function.BooleanSupplier;

import com.yogpc.qp.machines.quarry.TileQuarry;
import com.yogpc.qp.machines.quarry.TileQuarry2;

class RangeWrapper {
    static final RangeWrapper infinity = new RangeWrapper(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, () -> false);
    final int xMax;
    final int xMin;
    final int yMax;
    final int yMin;
    final int zMax;
    final int zMin;
    private final BooleanSupplier isWaiting;

    public RangeWrapper(int xMax, int xMin, int yMax, int yMin, int zMax, int zMin, BooleanSupplier isWaiting) {
        this.xMax = xMax;
        this.xMin = xMin;
        this.yMax = yMax;
        this.yMin = yMin;
        this.zMax = zMax;
        this.zMin = zMin;
        this.isWaiting = isWaiting;
    }

    public RangeWrapper(TileQuarry quarry) {
        this(quarry.xMax, quarry.xMin, quarry.yMax, quarry.yMin, quarry.zMax, quarry.zMin,() -> quarry.G_getNow() == TileQuarry.Mode.NOT_NEED_BREAK);
    }

    public RangeWrapper(TileQuarry2 quarry2) {
        TileQuarry2.Area area = quarry2.area();
        this.xMax = area.xMax();
        this.xMin = area.xMin();
        this.yMax = area.yMax();
        this.yMin = area.yMin();
        this.zMax = area.zMax();
        this.zMin = area.zMin();
        isWaiting = () -> quarry2.action().mode() == TileQuarry2.waiting();
    }

    public static RangeWrapper of(Object tb) {
        if (tb instanceof TileQuarry) {
            return new RangeWrapper((TileQuarry) tb);
        } else if (tb instanceof TileQuarry2) {
            TileQuarry2 quarry2 = (TileQuarry2) tb;
            return new RangeWrapper(quarry2);
        } else
            return infinity;
    }

    public boolean waiting(){
        return isWaiting.getAsBoolean();
    }
}
