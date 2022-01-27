package com.yogpc.qp.machines.filler;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.yogpc.qp.QuarryPlusTest;
import com.yogpc.qp.machines.Area;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FillerTargetPosIteratorTest extends QuarryPlusTest {
    static final Area AREA = new Area(-3, 5, 10, 2, 8, 13, Direction.WEST);

    @Nested
    class BoxTest {
        @Test
        void instance() {
            var box = new FillerTargetPosIterator.Box(AREA);
            assertNotNull(box);
        }

        @Test
        void head() {
            var box = new FillerTargetPosIterator.Box(AREA);
            assertEquals(new BlockPos(-3, 5, 10), box.head());
        }

        @Test
        void last() {
            var box = new FillerTargetPosIterator.Box(AREA);
            var list = Lists.newArrayList(box);
            assertEquals(new BlockPos(2, 8, 13), list.get(list.size() - 1));
        }

        @Test
        void noDuplication() {
            var list = Lists.newArrayList(() -> new FillerTargetPosIterator.Box(AREA));
            var set = Set.copyOf(list);
            assertEquals(set.size(), list.size());
        }

        @Test
        void count() {
            var box = new FillerTargetPosIterator.Box(AREA);
            var list = Lists.newArrayList(box);
            assertEquals((AREA.maxX() - AREA.minX() + 1) * (AREA.maxY() - AREA.minY() + 1) * (AREA.maxZ() - AREA.minZ() + 1), list.size());
        }

        @Test
        void checkY() {
            var list = Lists.newArrayList(() -> new FillerTargetPosIterator.Box(AREA));
            for (int i = 5; i < 9; i++) {
                final int y = i;
                var atY = list.stream().filter(p -> p.getY() == y).count();
                assertEquals(list.size() / 4, atY);
            }
        }

        @Test
        void setCurrent1() {
            var pos = new BlockPos(2, 6, 13);
            var box = new FillerTargetPosIterator.Box(AREA);
            box.setCurrent(pos);
            assertEquals(pos, box.next());
            assertEquals(new BlockPos(-3, 7, 10), box.peek());
        }

        @Test
        void setCurrent2() {
            var pos = new BlockPos(1, 6, 11);
            var box = new FillerTargetPosIterator.Box(AREA);
            box.setCurrent(pos);
            assertEquals(pos, box.next());
            assertEquals(new BlockPos(2, 6, 11), box.peek());
        }

        @Test
        void setCurrent3() {
            var pos = new BlockPos(-3, 6, 10);
            var box = new FillerTargetPosIterator.Box(AREA);
            box.setCurrent(pos);
            assertEquals(pos, box.next());
            assertEquals(new BlockPos(-2, 6, 10), box.peek());
        }
    }

    @Nested
    class MiniBoxTest {
        static final Area AREA = new Area(1, 3, -2, 1, 6, -2, Direction.WEST);

        @Test
        void count() {
            var box = new FillerTargetPosIterator.Box(AREA);
            var list = Lists.newArrayList(box);
            assertEquals(4, list.size());
        }

        @Test
        void contents() {
            var list = Lists.newArrayList(() -> new FillerTargetPosIterator.Box(AREA));
            var expected = List.of(
                new BlockPos(1, 3, -2),
                new BlockPos(1, 4, -2),
                new BlockPos(1, 5, -2),
                new BlockPos(1, 6, -2)
            );
            assertIterableEquals(expected, list);
        }
    }
}
