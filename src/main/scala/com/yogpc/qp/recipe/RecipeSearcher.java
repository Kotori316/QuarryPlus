package com.yogpc.qp.recipe;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public interface RecipeSearcher {
    List<WorkbenchRecipe> getRecipe(List<ItemStack> inputs);

    WorkbenchRecipe dummyRecipe();

    scala.collection.Map<ResourceLocation, WorkbenchRecipe> getRecipeMap();

    RecipeSearcher getDefault = WorkbenchRecipe$.MODULE$;
}
