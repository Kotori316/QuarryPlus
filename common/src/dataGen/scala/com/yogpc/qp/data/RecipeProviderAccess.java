package com.yogpc.qp.data;

import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

public final class RecipeProviderAccess extends RecipeProvider {
    RecipeProviderAccess(HolderLookup.Provider provider, RecipeOutput recipeOutput) {
        super(provider, recipeOutput);
    }

    @Override
    protected void buildRecipes() {
    }

    public static Criterion<InventoryChangeTrigger.TriggerInstance> hasItem(ItemLike itemLike, RecipeProviderAccess provider) {
        return provider.has(itemLike);
    }

    public static Criterion<InventoryChangeTrigger.TriggerInstance> hasTag(TagKey<Item> tag, RecipeProviderAccess provider) {
        return provider.has(tag);
    }
}
