package com.yogpc.qp.machine;

import com.yogpc.qp.BeforeMC;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MachineStorageHolderTest extends BeforeMC {
    @Test
    void findForConstant() {
        var storage = new MachineStorageHolder.Constant(new MachineStorage());
        var holder = MachineStorageHolder.getHolder(storage);
        assertNotNull(holder);
        assertTrue(holder.isPresent());
    }

    @Test
    void findForObject() {
        var storage = new Object();
        var holder = MachineStorageHolder.getHolder(storage);
        assertNotNull(holder);
        assertFalse(holder.isPresent());
    }

    @Nested
    class AssumptionTest {
        @Test
        void same() {
            assertTrue(String.class.isAssignableFrom(String.class));
        }

        @Test
        void child() {
            assertTrue(Object.class.isAssignableFrom(String.class));
        }

        @Test
        void parent() {
            assertFalse(String.class.isAssignableFrom(Object.class));
        }
    }
}
