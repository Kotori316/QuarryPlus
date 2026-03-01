package com.yogpc.qp.forge.data;

import com.mojang.serialization.Codec;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.data.AbstractTestGenerator;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

final class TestEnvironmentGeneratorForge extends AbstractTestGenerator<TestEnvironmentDefinition> {

    public TestEnvironmentGeneratorForge(@NotNull PackOutput packOutput, @NotNull CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super("TestEnvironmentGeneratorForge", packOutput, "test_environment", lookupProvider);
    }

    @Override
    protected @NotNull Map<Identifier, TestEnvironmentDefinition> getValueMap(HolderLookup.Provider lookup) {
        return Map.of(
            Identifier.fromNamespaceAndPath(QuarryPlus.modID, "test"), new TestEnvironmentDefinition.AllOf()
        );
    }

    @Override
    protected @NotNull Codec<TestEnvironmentDefinition> codec() {
        return TestEnvironmentDefinition.DIRECT_CODEC;
    }

    @Override
    protected @NotNull ResourceKey<? extends Registry<TestEnvironmentDefinition>> targetKey() {
        return Registries.TEST_ENVIRONMENT;
    }
}
