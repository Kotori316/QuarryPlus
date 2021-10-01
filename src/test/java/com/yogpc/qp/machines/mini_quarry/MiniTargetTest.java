package com.yogpc.qp.machines.mini_quarry;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.yogpc.qp.QuarryPlusTest;
import com.yogpc.qp.machines.Area;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MiniTargetTest extends QuarryPlusTest {
    @Test
    void itemCount() {
        // (1, 4, 7) -> (2, 8, 10)
        var area = new Area(0, 4, 6, 3, 8, 11, Direction.NORTH);
        var list = Lists.newArrayList(new MiniTarget(area));
        assertEquals(2 * 5 * 4, list.size());
    }

    @Test
    void firstY() {
        var area = new Area(0, 4, 6, 3, 8, 11, Direction.NORTH);
        var iterator = new MiniTarget(area);
        assertEquals(8, iterator.next().getY());
    }

    @Test
    void lastY() {
        var area = new Area(0, 4, 6, 3, 8, 11, Direction.NORTH);
        var iterator = new MiniTarget(area);
        var last = Iterators.getLast(iterator);
        assertEquals(4, last.getY());
    }
}
