package com.yogpc.qp.gametest;

import com.google.common.base.CaseFormat;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Stream;

public final class GameTestFunctions {
    public static List<TestFunction> createTestFunctions(String batchName, String structureName) {
        var classes = List.of(
            LoadRecipeTest.class
        );
        return classes.stream()
            .flatMap(c -> Stream.of(c.getDeclaredMethods()))
            .filter(m -> (m.getModifiers() & Modifier.STATIC) == Modifier.STATIC)
            .filter(m -> m.getParameterCount() == 1)
            .filter(m -> m.getParameterTypes()[0] == GameTestHelper.class)
            .filter(m -> m.getReturnType() == void.class)
            .peek(m -> m.setAccessible(true))
            .map(m ->
                new TestFunction(batchName, CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, m.getName()), structureName, 100, 0, true, g -> {
                    try {
                        m.invoke(null, g);
                    } catch (ReflectiveOperationException e) {
                        throw new AssertionError(e);
                    }
                })
            ).toList();
    }
}
