package com.yogpc.qp.forge.data

import com.yogpc.qp.data.Recipe
import net.minecraft.advancements.Advancement
import net.minecraft.core.Registry
import net.minecraft.core.registries.{MultiRegistryBootstrap, Registries}
import net.minecraft.data.worldgen.BootstrapContext
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.crafting.Recipe as MCRecipe

class RecipeForge extends MultiRegistryBootstrap {
  override def requestedRegistries(): java.util.Set[ResourceKey[? <: Registry[?]]] = java.util.Set.of(Registries.RECIPE, Registries.ADVANCEMENT)

  override def run(registries: MultiRegistryBootstrap.BootstrapGetter): Unit = {
    given recipeContext: BootstrapContext[MCRecipe[?]] = registries.get(Registries.RECIPE)

    given advancementContext: BootstrapContext[Advancement] = registries.get(Registries.ADVANCEMENT)

    val itemRegistry = recipeContext.lookup(Registries.ITEM)
    val ingredientProvider = new IngredientProviderForge(itemRegistry)
    val recipeProvider = new Recipe(ingredientProvider)

    recipeProvider.buildRecipes()
  }
}
