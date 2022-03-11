package com.yogpc.qp.data;

import java.io.IOException;
import java.util.List;

import com.google.gson.GsonBuilder;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import org.apache.logging.log4j.Logger;

abstract class QuarryDataProvider implements DataProvider {
    private static final Logger LOGGER = QuarryPlus.getLogger(QuarryPlusDataProvider.class);
    protected final DataGenerator generatorIn;

    protected QuarryDataProvider(DataGenerator generatorIn) {
        this.generatorIn = generatorIn;
    }

    @Override
    public void run(HashCache cache) throws IOException {
        var path = generatorIn.getOutputFolder();
        var gson = new GsonBuilder().setPrettyPrinting().create();
        for (DataBuilder builder : data()) {
            var out = path.resolve("data/%s/%s/%s.json".formatted(builder.location().getNamespace(), directory(), builder.location().getPath()))
                .normalize()
                .toAbsolutePath();
            try {
                LOGGER.info("Generating {}", out);
                var json = builder.build();
                DataProvider.save(gson, cache, json, out);
            } catch (Exception e) {
                LOGGER.error("Failed to generate json for {}", builder);
            }
        }
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    abstract String directory();

    abstract List<? extends DataBuilder> data();
}
