package com.yogpc.qp.machine.marker;

import com.yogpc.qp.machine.Area;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NormalMarkerEntityTest {
    @Test
    void createLink() {
        assertDoesNotThrow(() -> new NormalMarkerEntity.Link(List.of()));
    }

    @Test
    void area() {
        var link = new NormalMarkerEntity.Link(List.of(
            new BlockPos(1, 1, 1),
            new BlockPos(4, 1, 3)
        ));
        assertEquals(new Area(1, 1, 1, 4, 5, 3, Direction.UP), link.area());
    }

    @Test
    void areaWithY() {
        var link = new NormalMarkerEntity.Link(List.of(
            new BlockPos(1, 1, 1),
            new BlockPos(4, 1, 3),
            new BlockPos(1, 3, 1)
        ));
        assertEquals(new Area(1, 1, 1, 4, 3, 3, Direction.UP), link.area());
    }

    @Test
    void threeMarkers() {
        var link = new NormalMarkerEntity.Link(List.of(
            new BlockPos(1, 1, 1),
            new BlockPos(1, 1, 8),
            new BlockPos(4, 1, 1)
        ));
        assertEquals(new Area(1, 1, 1, 4, 5, 8, Direction.UP), link.area());
    }
}
