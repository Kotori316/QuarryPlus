package com.kotori316.test_qp;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.yogpc.qp.utils.LoopList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoopListTest {
    LoopList<String> loopList;

    @BeforeEach
    void setUp() {
        List<String> content = Stream.of(
            "A", "B", "C", null, "E"
        ).collect(Collectors.toList());
        LoopList<String> loopList = new LoopList<>();
        loopList.setList(content);
        this.loopList = loopList;
    }

    @Test
    void get() {
        assertEquals("A", loopList.get(0));
        assertEquals("B", loopList.get(1));
        assertEquals("C", loopList.get(2));
        assertNull(loopList.get(3));
        assertEquals("E", loopList.get(4));
    }

    @Test
    void get2() {
        int size = loopList.size();
        assertEquals("A", loopList.get(size));
        assertEquals("B", loopList.get(size + 1));
        assertEquals("C", loopList.get(size + 2));
        assertNull(loopList.get(size + 3));
        assertEquals("E", loopList.get(size + 4));
    }

    @Test
    void getOptional() {
        int size = loopList.size();
        assertTrue(loopList.getOptional(0).isPresent());
        assertFalse(loopList.getOptional(3).isPresent());
        assertTrue(loopList.getOptional(size).isPresent());
        assertFalse(loopList.getOptional(3 + size).isPresent());
    }

    @Test
    void add() {
        loopList.add("F");
        int size = loopList.size();
        assertEquals("F", loopList.get(5));
        assertEquals("A", loopList.get(size));
        assertEquals("F", loopList.get(size + 5));
    }

    @Test
    void set() {
        loopList.set(3, "D");
        assertEquals("D", loopList.get(3));
        assertTrue(loopList.getOptional(3).isPresent());
    }

    @Test
    void emptyList() {
        LoopList<String> loopList = new LoopList<>();
        Supplier<String> supplier = () -> "Tame";
        for (int i = 0; i < 10; i++) {
            assertNull(loopList.get(i));
            assertFalse(loopList.getOptional(i).isPresent());
            assertEquals("Tame", loopList.getOrDefault(i, supplier));
        }
    }
}