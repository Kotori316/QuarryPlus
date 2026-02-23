package com.yogpc.qp.forge.data;

import com.mojang.serialization.Codec;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class TestInstanceGeneratorForge extends AbstractTestGenerator<GameTestInstance> {
    public TestInstanceGeneratorForge(@NotNull PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super("TestInstanceGeneratorForge", packOutput, "test_instance", lookupProvider);
    }

    @Override
    protected @NotNull Map<Identifier, GameTestInstance> getValueMap(HolderLookup.Provider lookup) {
        var def = lookup.lookupOrThrow(Registries.TEST_ENVIRONMENT).getOrThrow(ResourceKey.create(Registries.TEST_ENVIRONMENT, Identifier.fromNamespaceAndPath(QuarryPlus.modID, "test")));
        var testData = new TestData<Holder<TestEnvironmentDefinition>>(def, Identifier.fromNamespaceAndPath("minecraft", "empty"), 1, 0, true);
        return Map.of(
            Identifier.fromNamespaceAndPath(QuarryPlus.modID, "test"), new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, Identifier.fromNamespaceAndPath("minecraft", "always_pass")), testData)
        );
    }

    @Override
    protected Codec<GameTestInstance> codec() {
        return GameTestInstance.DIRECT_CODEC;
    }

    @Override
    protected ResourceKey<? extends Registry<GameTestInstance>> targetKey() {
        return Registries.TEST_INSTANCE;
    }
}
