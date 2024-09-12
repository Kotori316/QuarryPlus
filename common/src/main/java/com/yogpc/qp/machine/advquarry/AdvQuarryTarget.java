package com.yogpc.qp.machine.advquarry;

import com.yogpc.qp.machine.Area;
import com.yogpc.qp.machine.PickIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

abstract class AdvQuarryTarget extends PickIterator<BlockPos> {
    protected final Area area;
    protected final BlockPos lastPos;
    protected final BlockPos.MutableBlockPos cursor;

    AdvQuarryTarget(Area area, BlockPos lastPos) {
        this.area = area;
        this.lastPos = lastPos;
        cursor = new BlockPos.MutableBlockPos();
    }

    @Override
    public boolean hasNext() {
        return !lastPos.equals(lastReturned);
    }

    @Override
    public void setLastReturned(BlockPos lastReturned) {
        super.setLastReturned(lastReturned);
        cursor.set(lastReturned);
    }

    static BlockPos getFirstPos(Area area, Direction direction) {
        return switch (direction) {
            case NORTH, UP, DOWN -> new BlockPos(area.maxX() - 1, area.minY(), area.minZ() + 1);
            case SOUTH -> new BlockPos(area.minX() + 1, area.minY(), area.maxZ() - 1);
            case EAST -> new BlockPos(area.maxX() - 1, area.minY(), area.maxZ() - 1);
            case WEST -> new BlockPos(area.minX() + 1, area.minY(), area.minZ() + 1);
        };
    }

    static BlockPos getLastPos(Area area, Direction direction) {
        return getFirstPos(area, direction.getOpposite());
    }

    static class North extends AdvQuarryTarget {

        North(Area area) {
            super(area, getLastPos(area, Direction.NORTH));
        }

        @Override
        protected BlockPos update() {
            if (lastReturned == null) {
                cursor.set(getFirstPos(area, Direction.NORTH));
                return cursor;
            }
            if (lastReturned.getX() + 1 >= area.maxX()) {
                // Next z
                cursor.set(area.minX() + 1, area.minY(), lastReturned.getZ() + 1);
            } else {
                // Next x
                cursor.setX(lastReturned.getX() + 1);
            }
            return cursor;
        }
    }

    static class South extends AdvQuarryTarget {
        South(Area area) {
            super(area, getLastPos(area, Direction.SOUTH));
        }

        @Override
        protected BlockPos update() {
            if (lastReturned == null) {
                cursor.set(getFirstPos(area, Direction.SOUTH));
                return cursor;
            }
            if (lastReturned.getX() - 1 <= area.minX()) {
                // Next z
                cursor.set(area.maxX() - 1, area.minY(), lastReturned.getZ() - 1);
            } else {
                // Next x
                cursor.setX(lastReturned.getX() - 1);
            }
            return cursor;
        }
    }
}
