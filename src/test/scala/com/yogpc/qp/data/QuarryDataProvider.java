package com.yogpc.qp.data;

import java.io.IOException;
import java.util.List;

import com.google.gson.GsonBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;

abstract class QuarryDataProvider implements DataProvider {
    protected final DataGenerator generatorIn;

    protected QuarryDataProvider(DataGenerator generatorIn) {
        this.generatorIn = generatorIn;
    }

    @Override
    public void run(HashCache cache) throws IOException {
        var path = generatorIn.getOutputFolder();
        var gson = new GsonBuilder().setPrettyPrinting().create();
        for (DataBuilder builder : data()) {
            var out = path.resolve("data/%s/%s/%s.json".formatted(builder.location().getNamespace(), directory(), builder.location().getPath()));
            DataProvider.save(gson, cache, builder.build(), out);
        }
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    abstract String directory();

    abstract List<? extends DataBuilder> data();
}
