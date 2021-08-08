package com.yogpc.qp.utils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class ManualOrderTest {

    @Test
    void manualOrderTest1() {
        var order = ManualOrder.builder(Comparator.<Dummy>naturalOrder()).add(Dummy.SECOND).build();
        assertIterableEquals(List.of(Dummy.SECOND, Dummy.FIRST, Dummy.THIRD, Dummy.FOURTH), Arrays.stream(Dummy.values()).sorted(order).toList());
    }

    @Test
    void manualOrderTest2() {
        var order = ManualOrder.builder(Comparator.<Dummy>naturalOrder()).add(Dummy.THIRD).build();
        assertIterableEquals(List.of(Dummy.THIRD, Dummy.FIRST, Dummy.SECOND, Dummy.FOURTH), Arrays.stream(Dummy.values()).sorted(order).toList());
    }

    private enum Dummy {
        FIRST, SECOND, THIRD, FOURTH
    }
}
