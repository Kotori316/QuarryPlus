package com.yogpc.qp.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    void manualOrderTest3() {
        ManualOrder<Dummy> order = ManualOrder.<Dummy>builder(Comparator.naturalOrder()).add(Dummy.SECOND, 2).build();
        assertIterableEquals(List.of(Dummy.SECOND, Dummy.FIRST, Dummy.THIRD, Dummy.FOURTH), Arrays.stream(Dummy.values()).sorted(order).toList());
    }

    @Test
    void manualOrderTest4() {
        ManualOrder<Dummy> order = ManualOrder.<Dummy>builder(Comparator.naturalOrder()).add(Dummy.SECOND, 2).add(Dummy.FOURTH).build();
        assertIterableEquals(List.of(Dummy.SECOND, Dummy.FOURTH, Dummy.FIRST, Dummy.THIRD), Arrays.stream(Dummy.values()).sorted(order).toList());
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Test
    void manualOrderTest5() {
        var order = ManualOrder.<Character>builder(Comparator.naturalOrder())
            .add('a', 4)
            .add('b', 2)
            .add('z')
            .add('c')
            .add('d')
            .build();
        String expected = "bazcdefg";
        var chars = "abcdefgz".toCharArray();
        var result = new ArrayList<Character>();
        for (char c : chars) {
            result.add(c);
        }
        result.sort(order);
        assertEquals(expected, result.stream().map(Object::toString).collect(Collectors.joining()));
    }

    @Test
    void manualOrderTest6() {
        var order = ManualOrder.<String>builder(Comparator.naturalOrder())
            .add("INT") // 0
            .add("STRING") // 1
            .add("SHORT", 5)
            .add("BYTE") // 6
            .add("CHAR", 3)
            .build();
        var expected = List.of(
            "INT", "STRING", "CHAR", "SHORT", "BYTE", "BOOLEAN", "LONG", "SIGNED", "UNSIGNED"
        );
        assertEquals(expected,
            Stream.of(
                "BYTE", "CHAR", "INT", "STRING", "SHORT", "LONG", "UNSIGNED", "SIGNED", "BOOLEAN"
            ).sorted(order).toList());
    }

    private enum Dummy {
        FIRST, SECOND, THIRD, FOURTH
    }
}
