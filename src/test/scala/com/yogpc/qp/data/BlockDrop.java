package com.yogpc.qp.data;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import com.yogpc.qp.Holder;
import com.yogpc.qp.machines.module.ModuleLootFunction;
import com.yogpc.qp.machines.quarry.QuarryLootFunction;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.level.block.Block;

class BlockDrop extends QuarryDataProvider {
    protected BlockDrop(DataGenerator generatorIn) {
        super(generatorIn);
    }

    @Override
    String directory() {
        return "loot_tables/blocks";
    }

    @Override
    List<? extends DataBuilder> data() {
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
            Holder.BLOCK_SOLID_FUEL_QUARRY,
            Holder.BLOCK_FILLER,
            null
        ).filter(Objects::nonNull).map(LootTableSerializeHelper::withDrop);
        Stream<LootTableSerializeHelper> enchanted = Stream.<Block>of(
            Holder.BLOCK_MINI_QUARRY
        ).map(LootTableSerializeHelper::withEnchantedDrop);
        var quarry = LootTableSerializeHelper.withEnchantedDrop(Holder.BLOCK_QUARRY)
            .add(QuarryLootFunction.builder()).add(ModuleLootFunction.builder());
        var advPump = LootTableSerializeHelper.withEnchantedDrop(Holder.BLOCK_ADV_PUMP);
        var advQuarry = LootTableSerializeHelper.withEnchantedDrop(Holder.BLOCK_ADV_QUARRY)
            .add(ModuleLootFunction.builder());

        return Stream.of(notMachines, enchanted,
                Stream.of(quarry, advPump, advQuarry))
            .flatMap(Function.identity()).toList();
    }
}
