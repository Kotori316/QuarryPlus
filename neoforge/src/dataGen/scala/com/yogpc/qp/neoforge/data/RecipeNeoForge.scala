package com.yogpc.qp.neoforge.data

import com.yogpc.qp.data.Recipe
import net.minecraft.advancements.Advancement
import net.minecraft.core.Registry
import net.minecraft.core.registries.{MultiRegistryBootstrap, Registries}
import net.minecraft.data.worldgen.BootstrapContext
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.crafting.Recipe as McRecipe

import java.util.Set as JSet

object RecipeNeoForge extends MultiRegistryBootstrap {
  override def requestedRegistries(): JSet[ResourceKey[? <: Registry[?]]] =
    JSet.of(Registries.RECIPE, Registries.ADVANCEMENT)

  override def run(registries: MultiRegistryBootstrap.BootstrapGetter): Unit = {
    val recipeContext: BootstrapContext[McRecipe[?]] = registries.get(Registries.RECIPE)
    val advancementContext: BootstrapContext[Advancement] = registries.get(Registries.ADVANCEMENT)

    val ingredientProvider = IngredientProviderNeoForge(recipeContext.lookup(Registries.ITEM))
    new Recipe(ingredientProvider, recipeContext, advancementContext).buildRecipes()
  }
}
