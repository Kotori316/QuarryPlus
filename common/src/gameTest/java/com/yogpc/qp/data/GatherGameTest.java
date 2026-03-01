package com.yogpc.qp.data;

import com.kotori316.testutil.common.TestFunction;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

public interface GatherGameTest {
    Collection<GameTestProperty> gather();

    record GameTestProperty(Identifier id,
                            Function<Holder<TestEnvironmentDefinition>, TestData<Holder<TestEnvironmentDefinition>>> testData,
                            Consumer<GameTestHelper> test) {
        public GameTestProperty(TestFunction testFunction) {
            this(testFunction.name(), fromTestFunction(testFunction), testFunction.test());
        }

        static Function<Holder<TestEnvironmentDefinition>, TestData<Holder<TestEnvironmentDefinition>>> fromTestFunction(TestFunction testFunction) {
            return def -> new TestData<>(def, testFunction.structureName(), testFunction.maxTicks(), testFunction.setupTicks(), true);
        }

        public static Function<Holder<TestEnvironmentDefinition>, TestData<Holder<TestEnvironmentDefinition>>> empty() {
            return withStructure(Identifier.fromNamespaceAndPath("minecraft", "empty"));
        }

        public static Function<Holder<TestEnvironmentDefinition>, TestData<Holder<TestEnvironmentDefinition>>> withStructure(Identifier structureName) {
            return def -> new TestData<>(def, structureName, 1, 0, true);
        }
    }
}
