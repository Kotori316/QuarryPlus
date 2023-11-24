package com.yogpc.qp.machines;

public abstract class TargetIterator extends PickIterator<TargetIterator.XZPair> {
    protected final Area area;

    TargetIterator(Area area) {
        this.area = area;
        reset();
    }

    public static TargetIterator of(Area area) {
        return switch (area.direction()) {
            case NORTH, UP, DOWN -> new North(area);
            case SOUTH -> new South(area);
            case WEST -> new West(area);
            case EAST -> new East(area);
        };
    }

    public static TargetIterator of(Area area, boolean chunkByChunk) {
        if (chunkByChunk) return new ChunkByChunk(area);
        else return of(area);
    }

    @Override
    public final boolean hasNext() {
        return area.minX() < current.x() && current.x() < area.maxX() &&
            area.minZ() < current.z() && current.z() < area.maxZ();
    }

    public record XZPair(int x, int z) {
    }

    private static final class North extends TargetIterator {

        North(Area area) {
            super(area);
        }

        @Override
        protected XZPair update() {
            if (current.z() + 1 >= area.maxZ()) {
                // Next x
                return new XZPair(current.x() - 1, area.minZ() + 1);
            } else {
                return new XZPair(current.x(), current.z() + 1);
            }
        }

        @Override
        public XZPair head() {
            return new XZPair(area.maxX() - 1, area.minZ() + 1);
        }
    }

    private static final class South extends TargetIterator {

        South(Area area) {
            super(area);
        }

        @Override
        protected XZPair update() {
            if (current.z() - 1 <= area.minZ()) {
                // Next x
                return new XZPair(current.x() + 1, area.maxZ() - 1);
            } else {
                return new XZPair(current.x(), current.z() - 1);
            }
        }

        @Override
        public XZPair head() {
            return new XZPair(area.minX() + 1, area.maxZ() - 1);
        }
    }

    private static final class West extends TargetIterator {

        West(Area area) {
            super(area);
        }

        @Override
        protected XZPair update() {
            if (current.x() + 1 >= area.maxX()) {
                // Next z
                return new XZPair(area.minX() + 1, current.z() + 1);
            } else {
                return new XZPair(current.x() + 1, current.z());
            }
        }

        @Override
        public XZPair head() {
            return new XZPair(area.minX() + 1, area.minZ() + 1);
        }
    }

    private static final class East extends TargetIterator {

        East(Area area) {
            super(area);
        }

        @Override
        protected XZPair update() {
            if (current.x() - 1 <= area.minX()) {
                // Next z
                return new XZPair(area.maxX() - 1, current.z() - 1);
            } else {
                return new XZPair(current.x() - 1, current.z());
            }
        }

        @Override
        public XZPair head() {
            return new XZPair(area.maxX() - 1, area.maxZ() - 1);
        }
    }

    private static final class ChunkByChunk extends TargetIterator {

        /**
         * Exclusive.
         * Area(1, 1, 1, 5, 1, 5) will create iterator from (2, 1, 2) to (4, 1, 4)
         */
        ChunkByChunk(Area area) {
            super(area);
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
        protected XZPair update() {
            int currentChunkX = blockToSectionCoord(current.x());
            int currentChunkZ = blockToSectionCoord(current.z());
            var maxX = area.maxX() - 1;
            var maxZ = area.maxZ() - 1;
            var minX = area.minX() + 1;
            var minZ = area.minZ() + 1;
            if (current.x() + 1 <= Math.min(chunkToBlock(currentChunkX, 15), maxX)) {
                // Move x
                return new XZPair(current.x() + 1, current.z());
            }
            if (current.z() + 1 <= Math.min(chunkToBlock(currentChunkZ, 15), maxZ)) {
                // Move z
                // Reset x
                return new XZPair(Math.max(chunkToBlock(currentChunkX, 0), minX), current.z() + 1);
            }
            if (currentChunkX + 1 <= blockToSectionCoord(maxX)) {
                // Move chunk x
                // Reset z
                return new XZPair(
                    Math.max(chunkToBlock(currentChunkX + 1, 0), minX),
                    Math.max(chunkToBlock(currentChunkZ, 0), minZ));
            }
            if (currentChunkZ + 1 <= blockToSectionCoord(maxZ)) {
                // Move chunk z
                // Reset chunk x
                return new XZPair(
                    Math.max(chunkToBlock(blockToSectionCoord(minX), 0), minX),
                    Math.max(chunkToBlock(currentChunkZ + 1, 0), minZ));
            }
            // End, returns dummy.
            return new XZPair(current.x() + 1, current.z());
        }

        @Override
        public XZPair head() {
            return new XZPair(area.minX() + 1, area.minZ() + 1);
        }

        /**
         * Copied from {@link net.minecraft.core.SectionPos#blockToSectionCoord(int)}
         */
        private static int blockToSectionCoord(int absolutePos) {
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
