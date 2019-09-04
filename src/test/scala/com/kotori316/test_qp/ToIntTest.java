package com.kotori316.test_qp;

import org.junit.jupiter.api.Test;

import static com.yogpc.qp.utils.ProxyCommon.toInt;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ToIntTest {

    @Test
    void toIntTest() {
        assertEquals(0, toInt(0L));
        assertEquals(100, toInt(100L));
        assertEquals(-455, toInt(-455L));
        assertEquals(-55, toInt(-55L));
    }

    @Test
    void overflowsTest() {
        assertEquals(Integer.MAX_VALUE, toInt(Integer.MAX_VALUE));
        assertEquals(Integer.MIN_VALUE, toInt(Integer.MIN_VALUE));
        assertEquals(Integer.MIN_VALUE, toInt((long) Integer.MIN_VALUE - 1));
        assertEquals(Integer.MAX_VALUE, toInt((long) Integer.MAX_VALUE + 1));
        assertEquals(Integer.MAX_VALUE, toInt(Long.MAX_VALUE - 155131));
        assertEquals(Integer.MIN_VALUE, toInt(Long.MIN_VALUE + 155131));
    }
}