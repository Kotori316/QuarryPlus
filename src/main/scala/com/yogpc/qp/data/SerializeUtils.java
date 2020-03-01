package com.yogpc.qp.data;

import java.util.function.Function;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.yogpc.qp.machines.workbench.IngredientWithCount;
import net.minecraft.block.Block;
import net.minecraft.world.storage.loot.ItemLootEntry;
import net.minecraft.world.storage.loot.StandaloneLootEntry;
import net.minecraft.world.storage.loot.functions.ILootFunction;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import scala.jdk.javaapi.CollectionConverters;

@SuppressWarnings("SameParameterValue")
    // I don't know why this suppress is needed, but needed.
class SerializeUtils {
    static JsonArray serializeIngredients(scala.collection.Seq<IngredientWithCount> inputs) {
        return toJsonArray(inputs, IngredientWithCount::serializeJson);
    }

    static JsonArray makeConditionArray(scala.collection.immutable.List<ICondition> conditions) {
        return toJsonArray(conditions, CraftingHelper::serialize);
    }

    static <T> JsonArray toJsonArray(scala.collection.Seq<T> data, Function<T, JsonElement> toJson) {
        return CollectionConverters.asJava(data).stream()
            .map(toJson)
            .reduce(new JsonArray(), (a, d) -> {
                a.add(d);
                return a;
            }, (a1, a2) -> {
                a1.addAll(a2);
                return a1;
            });
    }

    static StandaloneLootEntry.Builder<?> builder(Block block, scala.collection.Seq<ILootFunction.IBuilder> functions) {
        StandaloneLootEntry.Builder<?> b = ItemLootEntry.builder(block);
        CollectionConverters.asJava(functions).forEach(b::acceptFunction);
        return b;
    }
}
