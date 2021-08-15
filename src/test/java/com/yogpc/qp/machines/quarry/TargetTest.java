package com.yogpc.qp.machines.quarry;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.yogpc.qp.machines.Area;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TargetTest {
    static final Area area = new Area(1, 0, 3, 5, 4, 6, Direction.UP);

    @Test
    void frame1() {
        var targetList = getAllPos(() -> new FrameTarget(area)).toList();
        assertAll("Contains Edge Pos",
            IntStream.rangeClosed(area.minY(), area.maxY()).boxed().flatMap(
                y -> Stream.of(
                    new BlockPos(1, y, 3),
                    new BlockPos(5, y, 3),
                    new BlockPos(1, y, 6),
                    new BlockPos(5, y, 6)
                )
            ).map(p -> () -> assertTrue(targetList.contains(p), "Pos: " + p))
        );
    }

    @Test
    void frame3() {
        var targetList = getAllPos(() -> new FrameTarget(area)).toList();
        assertAll("Not Contains Upper Edge Pos",
            IntStream.of(area.minY() - 1, area.maxY() + 1).boxed().flatMap(
                y -> Stream.of(
                    new BlockPos(1, y, 3),
                    new BlockPos(5, y, 3),
                    new BlockPos(1, y, 6),
                    new BlockPos(5, y, 6)
                )
            ).map(p -> () -> assertFalse(targetList.contains(p), "Pos: " + p))
        );
    }

    @Test
    void frame2() {
        var targetList = getAllPos(() -> new FrameTarget(area)).toList();
        assertAll("Not Contains Inside Pos",
            BlockPos.stream(area.minX() + 1, area.minY(), area.minZ() + 1, area.maxX() - 1, area.maxY(), area.maxZ() - 1)
                .map(BlockPos::toImmutable)
                .map(p -> () -> assertFalse(targetList.contains(p), "Pos: " + p))
        );
        assertFalse(targetList.contains(new BlockPos(5, 3, 4)));
    }

    static Stream<BlockPos> getAllPos(Supplier<Target> targetSupplier) {
        var target = targetSupplier.get();
        return Stream.generate(() -> {
            var p = target.get(true);
            if (p == null) return null;
            else return p.toImmutable();
        }).takeWhile(Objects::nonNull);
    }

    @Test
    void inside0() {
        assertEquals(10, getAllPos(() -> Target.newFrameInside(area, area.minY(), area.maxY())).limit(10).distinct().count());
    }

    @Test
    void inside1() {
        var poses = assertTimeout(Duration.ofSeconds(5), () -> getAllPos(() -> Target.newFrameInside(area, area.minY(), area.maxY())).toList());
        assertEquals(30, poses.size());

        assertAll(
            () -> assertEquals(new BlockPos(2, 4, 4), poses.get(0)),
            () -> assertEquals(new BlockPos(2, 4, 5), poses.get(1)),
            () -> assertEquals(new BlockPos(3, 4, 4), poses.get(2)),
            () -> assertEquals(new BlockPos(3, 4, 5), poses.get(3)),
            () -> assertEquals(new BlockPos(4, 4, 4), poses.get(4)),
            () -> assertEquals(new BlockPos(2, 3, 4), poses.get(6)),
            () -> assertEquals(new BlockPos(2, 2, 4), poses.get(12)),
            () -> assertEquals(new BlockPos(2, 1, 4), poses.get(18)),
            () -> assertEquals(new BlockPos(2, 0, 4), poses.get(24)),
            () -> assertEquals(new BlockPos(4, 4, 5), poses.get(5)),
            () -> assertEquals(new BlockPos(4, 3, 5), poses.get(11)),
            () -> assertEquals(new BlockPos(4, 2, 5), poses.get(17)),
            () -> assertEquals(new BlockPos(4, 1, 5), poses.get(23)),
            () -> assertEquals(new BlockPos(4, 0, 5), poses.get(29))
        );
    }
}
