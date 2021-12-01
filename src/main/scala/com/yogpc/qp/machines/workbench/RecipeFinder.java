package com.yogpc.qp.machines.workbench;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.server.ServerLifecycleHooks;

public interface RecipeFinder {
    Map<ResourceLocation, WorkbenchRecipe> recipes();

    default int recipeSize() {
        return recipes().size();
    }

    default List<WorkbenchRecipe> getRecipes(List<ItemStack> input) {
        return recipes().values().stream()
            .filter(r -> r.hasAllRequiredItems(input))
            .sorted(WorkbenchRecipe.COMPARATOR)
            .toList();
    }

    default List<WorkbenchRecipe> findRecipes(ItemStack output) {
        if (output.isEmpty()) return Collections.emptyList();
        return recipes().values().stream()
            .filter(r -> ItemStack.isSameItemSameTags(r.getResultItem(), output))
            .toList();
    }

    @SuppressWarnings({"SameParameterValue"})
    static <C extends Container, T extends Recipe<C>> Map<ResourceLocation, T> find(RecipeManager manager, RecipeType<T> recipeType) {
        return manager.getAllRecipesFor(recipeType).stream()
            .map(recipe -> Map.entry(recipe.getId(), recipe))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}

class DefaultFinder implements RecipeFinder {

    @Override
    public Map<ResourceLocation, WorkbenchRecipe> recipes() {
        return rawRecipeMap().entrySet().stream()
            .filter(e -> e.getValue().hasContent())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<ResourceLocation, WorkbenchRecipe> rawRecipeMap() {
        return Optional.ofNullable(ServerLifecycleHooks.getCurrentServer())
            .map(MinecraftServer::getRecipeManager)
            .map(r -> RecipeFinder.find(r, WorkbenchRecipe.RECIPE_TYPE))
            .orElse(Collections.emptyMap());
    }

}
