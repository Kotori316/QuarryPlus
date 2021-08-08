package com.yogpc.qp.machines.quarry;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.yogpc.qp.machines.Area;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
}
