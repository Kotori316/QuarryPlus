package com.yogpc.qp.integration.ftbchunks;

import java.util.Set;
import java.util.stream.Collectors;

import com.yogpc.qp.machines.Area;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FTBChunksProtectionCheckTest {
    @Nested
    class ChunkPosStreamTest {
        @ParameterizedTest
        @ValueSource(ints = {0, 1, 15, 16, 100, -1, -64})
        void justOne1(int maxY) {
            Area area = new Area(0, -64, 0, 0, maxY, 0, Direction.NORTH);
            var chunkPoses = FTBChunksProtectionCheck.getChunkPosStream(area).collect(Collectors.toSet());

            assertEquals(Set.of(new ChunkPos(0, 0)), chunkPoses);
        }

        @Test
        void justOne2() {
            Area area = new Area(0, 0, 0, 15, 0, 0, Direction.NORTH);
            var chunkPoses = FTBChunksProtectionCheck.getChunkPosStream(area).collect(Collectors.toSet());

            assertEquals(Set.of(new ChunkPos(0, 0)), chunkPoses);
        }

        @Test
        void x2() {
            Area area = new Area(0, 0, 0, 16, 0, 0, Direction.NORTH);
            var chunkPoses = FTBChunksProtectionCheck.getChunkPosStream(area).collect(Collectors.toSet());

            assertEquals(Set.of(new ChunkPos(0, 0), new ChunkPos(1, 0)), chunkPoses);
        }

        @Test
        void z2() {
            Area area = new Area(0, 0, 0, 0, 0, 16, Direction.NORTH);
            var chunkPoses = FTBChunksProtectionCheck.getChunkPosStream(area).collect(Collectors.toSet());

            assertEquals(Set.of(new ChunkPos(0, 0), new ChunkPos(0, 1)), chunkPoses);
        }

        @Test
        void xz1() {
            Area area = new Area(0, 0, 0, 15, 0, 15, Direction.NORTH);
            var chunkPoses = FTBChunksProtectionCheck.getChunkPosStream(area).collect(Collectors.toSet());

            assertEquals(Set.of(new ChunkPos(0, 0)), chunkPoses);
        }

        @Test
        void xz2() {
            Area area = new Area(0, 0, 0, 16, 0, 16, Direction.NORTH);
            var chunkPoses = FTBChunksProtectionCheck.getChunkPosStream(area).collect(Collectors.toSet());

            assertEquals(Set.of(new ChunkPos(0, 0), new ChunkPos(0, 1), new ChunkPos(1, 0), new ChunkPos(1, 1)), chunkPoses);
        }
    }
}