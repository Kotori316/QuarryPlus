package com.yogpc.qp.machines.advquarry;

import java.util.function.Predicate;
import java.util.stream.Stream;

import com.yogpc.qp.QuarryPlusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(QuarryPlusTest.class)
class AdvQuarryActionTest {
    @Test
    void dummy() {
        var clazz = AdvQuarryAction.class;
        var declaredClasses = clazz.getDeclaredClasses();
        assertTrue(declaredClasses.length > 0);
        assertTrue(serializerKeys().findAny().isPresent());
        assertTrue(actionClasses().findAny().isPresent());
    }

    @ParameterizedTest
    @MethodSource("serializerKeys")
    @DisplayName("Serializer's key must be a class.")
    void serializer1(String key) {
        assertTrue(Stream.of(AdvQuarryAction.class.getDeclaredClasses())
            .map(Class::getSimpleName)
            .anyMatch(Predicate.isEqual(key)));
    }

    @ParameterizedTest
    @MethodSource("actionClasses")
    @DisplayName("Action class must have own serializer class")
    void serializer2(Class<?> clazz) {
        var serializerName = clazz.getSimpleName() + "Serializer";
        assertAll(
            () -> assertTrue(AdvQuarryAction.SERIALIZER_MAP.containsKey(clazz.getSimpleName())),
            () -> assertTrue(Stream.of(AdvQuarryAction.class.getDeclaredClasses())
                .map(Class::getSimpleName)
                .anyMatch(Predicate.isEqual(serializerName)))
        );
    }

    static Stream<String> serializerKeys() {
        return AdvQuarryAction.SERIALIZER_MAP.keySet().stream();
    }

    static Stream<Class<?>> actionClasses() {
        return Stream.of(AdvQuarryAction.class.getDeclaredClasses())
            .filter(c -> c.getSuperclass() == AdvQuarryAction.class);
    }
}
