package com.yogpc.qp.data;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

abstract class QuarryDataProvider implements DataProvider {
    private static final Logger LOGGER = QuarryPlus.getLogger(QuarryPlusDataProvider.class);
    protected final DataGenerator generatorIn;
    protected final CompletableFuture<HolderLookup.Provider> provider;

    protected QuarryDataProvider(DataGenerator generatorIn, CompletableFuture<HolderLookup.Provider> provider) {
        this.generatorIn = generatorIn;
        this.provider = provider;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        var path = generatorIn.getPackOutput().getOutputFolder();
        return CompletableFuture.allOf(
            data().stream().map(b -> outputWork(cache, path, b))
                .toArray(CompletableFuture[]::new)
        );
    }

    private CompletableFuture<?> outputWork(CachedOutput cache, Path path, DataBuilder builder) {
        return this.provider.thenCompose(p -> {
            Path out = path.resolve("data/%s/%s/%s.json".formatted(builder.location().getNamespace(), directory(), builder.location().getPath()))
                .normalize()
                .toAbsolutePath();
            var json = builder.build(p);
            return DataProvider.saveStable(cache, json, out);
        });
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    abstract String directory();

    abstract List<? extends DataBuilder> data();
}
