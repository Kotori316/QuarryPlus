package com.yogpc.qp.machine.marker;

import com.electronwill.nightconfig.core.Config;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.PlatformAccessDelegate;
import com.yogpc.qp.machine.Area;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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
        assertEquals(new Area(1, 1, 1, 4, 1, 3, Direction.UP), link.area());
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
        assertEquals(new Area(1, 1, 1, 4, 1, 8, Direction.UP), link.area());
    }

    @Nested
    class ConfigTest {
        @BeforeEach
        void setUp() {
            var delegate = (PlatformAccessDelegate) PlatformAccess.getAccess();
            delegate.reset();
        }

        @Test
        void rangeDefault() {
            assertEquals(256, PlatformAccess.config().markerPlusRange());
        }

        @Test
        void range128() {
            var delegate = (PlatformAccessDelegate) PlatformAccess.getAccess();
            var c = Config.inMemory();
            c.set("markerPlusRange", 128);
            delegate.setConfig(c, false);

            assertEquals(128, PlatformAccess.config().markerPlusRange());
        }
    }
}
