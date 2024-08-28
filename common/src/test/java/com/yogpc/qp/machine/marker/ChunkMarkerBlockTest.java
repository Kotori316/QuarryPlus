package com.yogpc.qp.machine.marker;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChunkMarkerBlockTest {
    @Nested
    class InRangeTest {
        @ParameterizedTest
        @MethodSource
        void testInRange(double expected, double value, double min, double max) {
            var result = ChunkMarkerBlock.inRange(value, min, max);
            assertEquals(expected, result);
        }

        static Arguments arg(double expected, double value, double min, double max) {
            return Arguments.of(expected, value, min, max);
        }

        static Stream<Arguments> testInRange() {
            return Stream.of(
                arg(359, -1, 0, 360),
                arg(1, 361, 0, 360),
                arg(0, 360, 0, 360),
                arg(36, 36, 0, 360),
                arg(0, 0, 0, 360),
                arg(8, -2, 0, 10),
                arg(6, -3, 0, 9),
                arg(6, -3, 1, 10),
                arg(9, 0, 1, 10),
                arg(-8, -8, -10, -5)
            );
        }
    }
}
