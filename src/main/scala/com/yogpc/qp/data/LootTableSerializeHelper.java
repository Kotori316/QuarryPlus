package com.yogpc.qp.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.JsonElement;
import com.yogpc.qp.machines.EnchantedLootFunction;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

class LootTableSerializeHelper extends BlockLoot implements QuarryPlusDataProvider.DataBuilder {
    private final Block block;
    private final List<LootItemFunction.Builder> functions;

    LootTableSerializeHelper(Block block, List<LootItemFunction.Builder> functions) {
        this.block = block;
        this.functions = functions;
    }

    @Override
    public ResourceLocation location() {
        return block.getRegistryName();
    }

    @Override
    public JsonElement build() {
        LootPoolSingletonContainer.Builder<?> value = LootItem.lootTableItem(block);
        functions.forEach(value::apply);
        var builder = LootTable.lootTable()
            .withPool(BlockLoot.applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(value)));
        builder.setParamSet(LootContextParamSets.BLOCK);
        return LootTables.serialize(builder.build());
    }

    LootTableSerializeHelper add(LootItemFunction.Builder builder) {
        var copy = new ArrayList<>(functions);
        copy.add(builder);
        return new LootTableSerializeHelper(block, copy);
    }

    @Override
    public String toString() {
        return "LootTableSerializeHelper{" +
            "block=" + block +
            ", functions=" + functions +
            '}';
    }

    static LootTableSerializeHelper withDrop(Block block) {
        return new LootTableSerializeHelper(block, Collections.emptyList());
    }

    static LootTableSerializeHelper withEnchantedDrop(Block block) {
        return new LootTableSerializeHelper(block, Collections.singletonList(EnchantedLootFunction.builder()));
    }
}
