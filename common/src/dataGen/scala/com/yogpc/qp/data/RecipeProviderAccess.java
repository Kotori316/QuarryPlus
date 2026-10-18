package com.yogpc.qp.data;

import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;

public final class RecipeProviderAccess extends RecipeProvider {
    RecipeProviderAccess(BootstrapContext<Recipe<?>> recipeContext, BootstrapContext<Advancement> advancementContext) {
        super(recipeContext, advancementContext);
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
