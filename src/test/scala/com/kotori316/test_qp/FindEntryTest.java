package com.kotori316.test_qp;

import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import static com.yogpc.qp.machines.pb.PlacerTile.findEntry;
import static jp.t2v.lab.syntax.MapStreamSyntax.always_false;
import static jp.t2v.lab.syntax.MapStreamSyntax.always_true;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FindEntryTest {
    @Test
    void empty() {
        List<String> strings = Collections.emptyList();
        assertEquals(OptionalInt.empty(), findEntry(strings, always_true(), 0));
    }

    @Test
    void singleton() {
        List<String> one = Collections.singletonList("one");
        assertEquals(OptionalInt.of(0), findEntry(one, always_true(), 0));
        assertEquals(OptionalInt.empty(), findEntry(one, always_false(), 0));
        assertEquals(OptionalInt.empty(), findEntry(one, always_true(), 1));
    }

    @Test
    void ten() {
        List<Integer> ten = IntStream.range(0, 10).boxed().collect(Collectors.toList());
        assertEquals(OptionalInt.of(0), findEntry(ten, i -> i % 2 == 0, 0));
        assertEquals(OptionalInt.of(2), findEntry(ten, i -> i % 2 == 0, 1));
        assertEquals(OptionalInt.of(2), findEntry(ten, i -> i % 2 == 0, 2));
        assertEquals(OptionalInt.of(4), findEntry(ten, i -> i % 2 == 0, 3));
        assertEquals(OptionalInt.of(4), findEntry(ten, i -> i % 2 == 0, 4));
        assertEquals(OptionalInt.of(5), findEntry(ten, i -> i % 5 == 0, 1));
    }

    @Test
    void loop() {
        List<Integer> ten = IntStream.rangeClosed(1, 10).boxed().collect(Collectors.toList());
        assertEquals(OptionalInt.empty(), findEntry(ten, Predicate.isEqual(0), 0));
        assertEquals(OptionalInt.of(0), findEntry(ten, Predicate.isEqual(1), 0));
        assertEquals(OptionalInt.of(0), findEntry(ten, Predicate.isEqual(1), 2));
        assertEquals(OptionalInt.of(0), findEntry(ten, Predicate.isEqual(1), 9));
    }
}
