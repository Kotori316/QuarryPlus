package com.yogpc.qp.machine.module;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class QuarryModuleTest {
    @TestFactory
    Stream<DynamicTest> testQuarryModule() {
        return Stream.of(QuarryModule.Constant.values())
            .map(m -> DynamicTest.dynamicTest(m.toString(), () -> assertNotNull(assertDoesNotThrow(m::moduleId))));
    }
}
