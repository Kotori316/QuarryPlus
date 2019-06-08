package com.kotori316.test_qp;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.yogpc.qp.utils.NoDuplicateList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NoDuplicateListTest {
    // contains 0 - 99
    NoDuplicateList<Integer> list;

    @BeforeEach
    void setUp() {
        list = NoDuplicateList.create();
        IntStream.range(0, 100).boxed().forEach(list::add);
        assertEquals(100, list.size());
    }

    @Test
    void add() {
        int size0 = list.size();
        list.add(0);
        assertEquals(size0, list.size());
        assertTrue(list.add(-4));
        assertEquals(size0 + 1, list.size());
    }

    @Test
    void addAll() {
        List<Integer> collect = IntStream.range(-50, 50).boxed().collect(Collectors.toList());
        list.addAll(collect);
        assertEquals(150, list.size());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void get() {
        int size0 = list.size();
        list.add(0);
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(size0));
        assertEquals(64, list.get(64));
    }
}