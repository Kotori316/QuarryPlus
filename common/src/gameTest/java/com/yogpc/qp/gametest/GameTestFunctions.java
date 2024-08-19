package com.yogpc.qp.gametest;

import com.google.common.base.CaseFormat;
import com.yogpc.qp.machine.mover.PlaceMoverTest;
import com.yogpc.qp.machine.quarry.PlaceQuarryTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class GameTestFunctions {
    public static List<TestFunction> createTestFunctionsNoPlace(String batchName, String structureName) {
        List<Class<?>> classes = List.of(
            LoadRecipeTest.class,
            EnableMapTest.class
        );
        var fromClass = getTestFunctionStream(batchName, structureName, classes);
        return Stream.of(
            fromClass,
            AccessItemTest.accessItems(batchName, structureName),
            EnableMapTest.test(batchName, structureName)
        ).flatMap(Function.identity()).toList();
    }

    public static List<TestFunction> createTestFunctionsPlace(String batchName, String structureName) {
        List<Class<?>> classes = List.of(
            PlaceQuarryTest.class,
            PlaceMoverTest.class
        );
        var fromClass = getTestFunctionStream(batchName, structureName, classes);
        return Stream.of(
            fromClass,
            CheckBlockDropTest.checkDrops(batchName, structureName)
        ).flatMap(Function.identity()).toList();
    }

    private static @NotNull Stream<TestFunction> getTestFunctionStream(String batchName, String structureName, List<Class<?>> classes) {
        return classes.stream()
            .flatMap(c -> Stream.of(c.getDeclaredMethods()))
            .filter(Predicate.not(Method::isSynthetic))
            .filter(m -> (m.getModifiers() & Modifier.STATIC) == Modifier.STATIC)
            .filter(m -> (m.getModifiers() & Modifier.PRIVATE) != Modifier.PRIVATE)
            .filter(m -> m.getParameterCount() == 1)
            .filter(m -> m.getParameterTypes()[0] == GameTestHelper.class)
            .filter(m -> m.getReturnType() == void.class)
            .peek(m -> m.setAccessible(true))
            .map(m ->
                new TestFunction(batchName, CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, "%s_%s".formatted(m.getDeclaringClass().getSimpleName(), m.getName())), structureName, 100, 0, true, g -> {
                    try {
                        m.invoke(null, g);
                    } catch (ReflectiveOperationException | AssertionError e) {
                        throw new RuntimeException(e);
                    }
                })
            );
    }

    public static Consumer<GameTestHelper> wrapper(Consumer<GameTestHelper> test) {
        return g -> {
            try {
                test.accept(g);
            } catch (AssertionError e) {
                throw new RuntimeException(e);
            }
        };
    }
}
