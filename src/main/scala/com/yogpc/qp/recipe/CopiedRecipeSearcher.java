package com.yogpc.qp.recipe;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import scala.collection.JavaConverters;
import scala.collection.Map;

import static jp.t2v.lab.syntax.MapStreamSyntax.entryToMap;
import static jp.t2v.lab.syntax.MapStreamSyntax.toEntry;

public class CopiedRecipeSearcher implements RecipeSearcher {
    private final Set<? extends WorkbenchRecipe> recipes;

    public CopiedRecipeSearcher(Set<? extends WorkbenchRecipe> recipes) {
        this.recipes = recipes;
    }

    @Override
    public List<WorkbenchRecipe> getRecipe(List<ItemStack> inputs) {
        return recipes.stream()
            .filter(WorkbenchRecipe::hasContent)
            .filter(r -> r.inputsJ().stream().allMatch(oneItem ->
                inputs.stream().anyMatch(stack -> oneItem.stream().anyMatch(i -> i.matches(stack)))
            ))
            .sorted(WorkbenchRecipe.recipeOrdering())
            .collect(Collectors.toList());
    }

    @Override
    public WorkbenchRecipe dummyRecipe() {
        return WorkbenchRecipe.dummyRecipe();
    }

    @Override
    public Map<ResourceLocation, WorkbenchRecipe> getRecipeMap() {
        return JavaConverters.mapAsScalaMapConverter(recipes.stream()
            .map(toEntry(WorkbenchRecipe::location, Function.<WorkbenchRecipe>identity()))
            .collect(entryToMap())).asScala();
    }
}
