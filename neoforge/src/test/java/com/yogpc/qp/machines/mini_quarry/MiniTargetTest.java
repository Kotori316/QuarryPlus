package com.yogpc.qp.machines.mini_quarry;

import java.util.Arrays;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.yogpc.qp.QuarryPlusTest;
import com.yogpc.qp.machines.Area;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(QuarryPlusTest.class)
class MiniTargetTest {
    @Nested
    class SingleTest {
        @Test
        void itemCount() {
            // (1, 4, 7) -> (2, 8, 10)
            var area = new Area(0, 4, 6, 3, 8, 11, Direction.NORTH);
            var list = Lists.newArrayList(MiniTarget.of(area, false));
            assertEquals(2 * 5 * 4, list.size());
        }

        @Test
        void firstY() {
            var area = new Area(0, 4, 6, 3, 8, 11, Direction.NORTH);
            var iterator = MiniTarget.of(area, false);
            assertEquals(8, iterator.next().getY());
        }

        @Test
        void lastY() {
            var area = new Area(0, 4, 6, 3, 8, 11, Direction.NORTH);
            var iterator = MiniTarget.of(area, false);
            var last = Iterators.getLast(iterator);
            assertEquals(4, last.getY());
        }
    }

    @Nested
    class RepeatTest {
        @Test
        void itemCount() {
            // (1, 4, 7) -> (2, 8, 10)
            var area = new Area(0, 4, 6, 3, 8, 11, Direction.NORTH);
            var list = Lists.newArrayList(MiniTarget.of(area, true));
            assertEquals(2 * 4 * 5 * 2, list.size());
        }

        @Test
        @DisplayName("From 14 To 10")
        void count2() {
            var area = new Area(3, 10, 6, 5, 14, 8, Direction.NORTH);
            var list = Lists.newArrayList(MiniTarget.of(area, true));
            assertEquals(10, list.size());
            assertArrayEquals(new int[]{14, 14, 13, 13, 12, 12, 11, 11, 10, 10},
                list.stream().mapToInt(BlockPos::getY).toArray());
        }

        @Test
        @DisplayName("From 15 To 10")
        void count3() {
            var area = new Area(3, 10, 6, 5, 15, 8, Direction.NORTH);
            var list = Lists.newArrayList(MiniTarget.of(area, true));
            var actual = list.stream().mapToInt(BlockPos::getY).toArray();
            assertArrayEquals(new int[]{15, 15, 14, 14, 13, 13, 12, 12, 11, 11, 10, 10},
                actual, String.format("Actual: %s", Arrays.toString(actual)));
        }

        @Test
        void firstY() {
            var area = new Area(0, 4, 6, 3, 8, 11, Direction.NORTH);
            var iterator = MiniTarget.of(area, true);
            assertEquals(8, iterator.next().getY());
        }

        @Test
        void lastY() {
            var area = new Area(0, 4, 6, 3, 8, 11, Direction.NORTH);
            var iterator = MiniTarget.of(area, true);
            var last = Iterators.getLast(iterator);
            assertEquals(4, last.getY());
        }
    }
}
