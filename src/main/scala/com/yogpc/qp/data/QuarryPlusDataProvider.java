package com.yogpc.qp.data;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.module.ModuleLootFunction;
import com.yogpc.qp.machines.quarry.QuarryLootFunction;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = QuarryPlus.modID)
public class QuarryPlusDataProvider {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        if (event.includeServer()) {
            event.getGenerator().addProvider(new BlockDrop(event.getGenerator()));
            event.getGenerator().addProvider(new Recipe(event.getGenerator()));
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

class BlockDrop extends QuarryPlusDataProvider.QuarryDataProvider {
    protected BlockDrop(DataGenerator generatorIn) {
        super(generatorIn);
    }

    @Override
    String directory() {
        return "loot_tables/blocks";
    }

    @Override
    List<? extends QuarryPlusDataProvider.DataBuilder> data() {
        var notMachines = Stream.of(
            Holder.BLOCK_CREATIVE_GENERATOR,
            Holder.BLOCK_16_MARKER,
            Holder.BLOCK_MARKER,
            Holder.BLOCK_16_MARKER,
            Holder.BLOCK_FLEX_MARKER,
            Holder.BLOCK_MINING_WELL,
            Holder.BLOCK_PUMP,
            Holder.BLOCK_WORKBENCH,
            Holder.BLOCK_MOVER,
            Holder.BLOCK_EXP_PUMP,
            Holder.BLOCK_PLACER,
            Holder.BLOCK_REPLACER,
            Holder.BLOCK_BOOK_MOVER,
            null
        ).filter(Objects::nonNull).map(LootTableSerializeHelper::withDrop);
        Stream<LootTableSerializeHelper> enchanted = Stream.<Block>of(
        ).map(LootTableSerializeHelper::withEnchantedDrop);
        var quarry = LootTableSerializeHelper.withEnchantedDrop(Holder.BLOCK_QUARRY)
            .add(QuarryLootFunction.builder()).add(ModuleLootFunction.builder());
        var advPump = LootTableSerializeHelper.withEnchantedDrop(Holder.BLOCK_ADV_PUMP)
            .add(ModuleLootFunction.builder());

        return Stream.of(notMachines, enchanted,
                Stream.of(quarry, advPump))
            .flatMap(Function.identity()).toList();
    }
}
