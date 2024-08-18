package com.yogpc.qp.data;

import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import java.util.concurrent.CompletableFuture;

public final class RecipeProviderAccess extends RecipeProvider {
    RecipeProviderAccess(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
    }

    public static Criterion<InventoryChangeTrigger.TriggerInstance> hasItem(ItemLike itemLike) {
        return RecipeProvider.has(itemLike);
    }

    public static Criterion<InventoryChangeTrigger.TriggerInstance> hasTag(TagKey<Item> tag) {
        return RecipeProvider.has(tag);
    }
}
