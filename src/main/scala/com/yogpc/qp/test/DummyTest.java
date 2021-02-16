package com.yogpc.qp.test;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

final class DummyTest {
    /**
     * @return x + y
     */
    static int adder(int x, int y) {
        return x + y;
    }

    @Test
    void dummy1() {
        assertEquals(5, adder(2, 3));
        assertEquals(8, adder(3, 5));
        assertNotEquals(4, adder(1, 8));
    }

    @Test
    @Disabled("Dummy test")
    void mustFail() {
        assertEquals(1, adder(4, 5));
    }
}
