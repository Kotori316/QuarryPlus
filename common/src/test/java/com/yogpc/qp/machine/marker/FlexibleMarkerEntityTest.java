package com.yogpc.qp.machine.marker;

import com.electronwill.nightconfig.core.Config;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.PlatformAccessDelegate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FlexibleMarkerEntityTest {

    @Nested
    class ConfigTest {
        @BeforeEach
        void setUp() {
            var delegate = (PlatformAccessDelegate) PlatformAccess.getAccess();
            delegate.reset();
        }

        @Test
        void rangeDefault() {
            assertEquals(256, PlatformAccess.config().flexibleMarkerRange());
        }

        @Test
        void range128() {
            var delegate = (PlatformAccessDelegate) PlatformAccess.getAccess();
            var c = Config.inMemory();
            c.set("flexibleMarkerRange", 128);
            delegate.setConfig(c, false);

            assertEquals(128, PlatformAccess.config().flexibleMarkerRange());
        }
    }
}
