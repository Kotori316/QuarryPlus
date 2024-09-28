package com.yogpc.qp.machine.advquarry;

import com.yogpc.qp.machine.Area;
import com.yogpc.qp.machine.PickIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

sealed abstract class AdvQuarryTarget extends PickIterator<BlockPos> {
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
        if (lastReturned != null) {
            cursor.set(lastReturned);
        }
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

    static final class North extends AdvQuarryTarget {

        North(Area area) {
            super(area, getLastPos(area, Direction.NORTH));
        }

        /**
         * +z, -x direction
         */
        @Override
        protected BlockPos update() {
            if (lastReturned == null) {
                cursor.set(getFirstPos(area, Direction.NORTH));
                return cursor;
            }
            if (lastReturned.getZ() + 1 < area.maxZ()) {
                // Next z
                cursor.setZ(lastReturned.getZ() + 1);
            } else {
                // Next x
                cursor.set(lastReturned.getX() - 1, lastReturned.getY(), area.minZ() + 1);
            }
            return cursor;
        }
    }

    static final class ChunkByChunk extends AdvQuarryTarget {

        /**
         * Exclusive.
         * Area(1, 1, 1, 5, 1, 5) will create iterator from (2, 1, 2) to (4, 1, 4)
         */
        ChunkByChunk(Area area) {
            super(area, new BlockPos(area.maxX() - 1, area.minY(), area.maxZ() - 1));
        }

        /*
        In Python
        def generate(first_pos, end_pos):
            current = first_pos
            max_pos = end_pos
            current_chunk = current[0] // 16, current[1] // 16
            first_chunk = current_chunk
            max_chunk = max_pos[0] // 16, max_pos[1] // 16
            yield current
            while True:
                # print(current)
                max_chunk_x = min((current_chunk[0] + 1) * 16, max_pos[0])
                if current[0] + 1 < max_chunk_x:
                    current = current[0] + 1, current[1]
                    yield current
                    continue
                # Change z
                max_chunk_z = min((current_chunk[1] + 1) * 16, max_pos[1])
                if current[1] + 1 < max_chunk_z:
                    current = max(current_chunk[0] * 16, first_pos[0]), current[1] + 1
                    yield current
                    continue
                # Change chunk X
                if current_chunk[0] + 1 <= max_chunk[0]:
                    current_chunk = current_chunk[0] + 1, current_chunk[1]
                    current = max(current_chunk[0] * 16, first_pos[0]), max(current_chunk[1] * 16, first_pos[1])
                    yield current
                    continue
                # Change chunk Z
                if current_chunk[1] + 1 <= max_chunk[1]:
                    current_chunk = first_chunk[0], current_chunk[1] + 1
                    current = max(current_chunk[0] * 16, first_pos[0]), max(current_chunk[1] * 16, first_pos[1])
                    yield current
                    continue
                # End?
                print(current)
                break
         */
        @Override
        protected BlockPos update() {
            if (lastReturned == null) {
                cursor.set(area.minX() + 1, area.minY(), area.minZ() + 1);
                return cursor;
            }
            int currentChunkX = blockToSectionCoordinate(lastReturned.getX());
            int currentChunkZ = blockToSectionCoordinate(lastReturned.getZ());
            var maxX = area.maxX() - 1;
            var maxZ = area.maxZ() - 1;
            var minX = area.minX() + 1;
            var minZ = area.minZ() + 1;
            if (lastReturned.getX() + 1 <= Math.min(chunkToBlock(currentChunkX, 15), maxX)) {
                // Move x
                cursor.set(lastReturned.getX() + 1, cursor.getY(), lastReturned.getZ());
                return cursor;
            }
            if (lastReturned.getZ() + 1 <= Math.min(chunkToBlock(currentChunkZ, 15), maxZ)) {
                // Move z
                // Reset x
                cursor.set(Math.max(chunkToBlock(currentChunkX, 0), minX), cursor.getY(), lastReturned.getZ() + 1);
                return cursor;
            }
            if (currentChunkX + 1 <= blockToSectionCoordinate(maxX)) {
                // Move chunk x
                // Reset z
                cursor.set(
                    Math.max(chunkToBlock(currentChunkX + 1, 0), minX),
                    cursor.getY(),
                    Math.max(chunkToBlock(currentChunkZ, 0), minZ)
                );
                return cursor;
            }
            if (currentChunkZ + 1 <= blockToSectionCoordinate(maxZ)) {
                // Move chunk z
                // Reset chunk x
                cursor.set(
                    Math.max(chunkToBlock(blockToSectionCoordinate(minX), 0), minX),
                    cursor.getY(),
                    Math.max(chunkToBlock(currentChunkZ + 1, 0), minZ)
                );
                return cursor;
            }
            // End, returns dummy.
            cursor.set(lastReturned.getX() + 1, cursor.getY(), lastReturned.getZ());
            return cursor;
        }

        /**
         * Copied from {@link net.minecraft.core.SectionPos#blockToSectionCoord(int)}
         */
        private static int blockToSectionCoordinate(int absolutePos) {
            return absolutePos >> 4;
        }

        /**
         * Copied from {@link net.minecraft.core.SectionPos#sectionToBlockCoord(int, int)}
         */
        private static int chunkToBlock(int chunk, int block) {
            return (chunk << 4) + block;
        }
    }
}
