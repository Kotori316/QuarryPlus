package com.yogpc.qp.data;

import java.io.IOException;
import java.util.List;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

// @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = QuarryPlus.modID)
public class QuarryPlusDataProvider {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        if (event.includeServer()) {
            event.getGenerator().addProvider(new BlockDrop(event.getGenerator()));
            event.getGenerator().addProvider(new Recipe(event.getGenerator()));
            event.getGenerator().addProvider(new RecipeAdvancement(event.getGenerator()));
        }
    }

    static ResourceLocation location(String path) {
        return new ResourceLocation(QuarryPlus.modID, path);
    }

    interface DataBuilder {
        ResourceLocation location();

        JsonElement build();
    }

    abstract static class QuarryDataProvider implements DataProvider {
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
}
