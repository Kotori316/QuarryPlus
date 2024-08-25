package com.yogpc.qp.machine.quarry;

import com.yogpc.qp.machine.Area;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuarryBlockTest {
    @Nested
    class AssumeAreaTest {
        @ParameterizedTest
        @MethodSource("lowDistance")
        void lowDistance(Direction direction, int maxY) {
            var area = new Area(0, 0, 0, 1, maxY, 3, direction);
            var modified = QuarryBlock.assumeY(area);
            var expected = new Area(0, 0, 0, 1, 4, 3, direction);
            assertEquals(expected, modified);
        }

        static Stream<Arguments> lowDistance() {
            return Stream.of(Direction.values())
                .flatMap(d -> IntStream.range(0, 5).mapToObj(y ->
                    Arguments.of(d, y)
                ));
        }

        @ParameterizedTest
        @MethodSource("longDistance")
        void longDistance(Direction direction, int maxY) {
            var area = new Area(3, 0, 0, 5, maxY, 3, direction);
            var modified = QuarryBlock.assumeY(area);
            assertEquals(area, modified);
        }

        static Stream<Arguments> longDistance() {
            return Stream.of(Direction.values())
                .flatMap(d -> IntStream.range(5, 7).mapToObj(y ->
                    Arguments.of(d, y)
                ));
        }
    }
}
