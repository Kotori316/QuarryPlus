package com.yogpc.qp.data;

import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.registries.RegistryPatchGenerator;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractTestGenerator<T> implements DataProvider {
    @NotNull
    private final String name;
    @NotNull
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;
    @NotNull
    private final PackOutput.PathProvider pathProvider;
    @Nullable
    private CompletableFuture<Map<Identifier, T>> cache;

    protected AbstractTestGenerator(@NotNull String name, @NotNull PackOutput packOutput, @NotNull String path, @NotNull CompletableFuture<HolderLookup.Provider> lookupProvider) {
        this.name = name;
        this.lookupProvider = lookupProvider;
        this.pathProvider = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, path);
    }

    @Override
    public final CompletableFuture<?> run(CachedOutput output) {
        return this.getCache().thenCompose(valueMap ->
            CompletableFuture.allOf(
                valueMap.entrySet().stream()
                    .map(entry -> this.save(output, entry.getKey(), entry.getValue()))
                    .toArray(CompletableFuture[]::new)
            )
        );
    }

    @NotNull
    protected abstract Map<Identifier, T> getValueMap(HolderLookup.Provider lookup);

    @NotNull
    private CompletableFuture<Map<Identifier, T>> getCache() {
        if (cache == null) {
            cache = this.lookupProvider.thenApply(this::getValueMap);
        }
        return cache;
    }

    @NotNull
    public final CompletableFuture<HolderLookup.Provider> getPatchedLookupProvider() {
        return getCache().thenCompose(valueMap -> {
            var registrySetBuilder = new RegistrySetBuilder();
            registrySetBuilder.add(targetKey(), context ->
                valueMap.forEach((identifier, t) -> context.register(ResourceKey.create(targetKey(), identifier), t))
            );

            return RegistryPatchGenerator.createLookup(this.lookupProvider, registrySetBuilder).thenApply(RegistrySetBuilder.PatchedRegistries::full);
        });
    }

    @NotNull
    private CompletableFuture<?> save(@NotNull CachedOutput output, @NotNull Identifier identifier, @NotNull T value) {
        return this.lookupProvider.thenCompose(lookupProvider ->
            DataProvider.saveStable(
                output,
                lookupProvider,
                codec(),
                value,
                this.pathProvider.json(identifier)
            )
        );
    }

    @NotNull
    protected abstract Codec<T> codec();

    @NotNull
    protected abstract ResourceKey<? extends Registry<T>> targetKey();

    @Override
    @NotNull
    public final String getName() {
        return name;
    }
}
