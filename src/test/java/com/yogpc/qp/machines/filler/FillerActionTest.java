package com.yogpc.qp.machines.filler;

import com.yogpc.qp.QuarryPlusTest;
import com.yogpc.qp.machines.Area;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FillerActionTest extends QuarryPlusTest {
    @Test
    void beforeStart() {
        var action = new FillerAction();
        assertNull(action.iterator);
        assertTrue(action.isFinished());
    }

    @Test
    void emptySerialize() {
        var action = new FillerAction();
        var tag = action.toNbt();
        var a2 = new FillerAction();
        a2.fromNbt(tag);
        assertNull(a2.iterator);
    }

    @Test
    void startedSerialize() {
        var area = new Area(0, 0, 0, 3, 3, 3, Direction.WEST);
        var action = new FillerAction();
        action.setIterator(new SkipIterator(area, FillerTargetPosIterator::box));
        var tag = action.toNbt();
        var a2 = new FillerAction();
        a2.fromNbt(tag);
        assertNotNull(a2.iterator);
        assertEquals(FillerTargetPosIterator.Box.class, a2.iterator.posIterator.getClass());
    }
}
