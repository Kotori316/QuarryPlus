package com.yogpc.qp.fabric.data

import com.yogpc.qp.data.Recipe
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.Registries
import net.minecraft.data.recipes.{RecipeOutput, RecipeProvider}
import net.minecraft.resources.ResourceLocation

import java.util.concurrent.CompletableFuture

final class RecipeFabric(output: FabricDataOutput, registries: CompletableFuture[HolderLookup.Provider]) extends FabricRecipeProvider(output, registries) {
  override def getRecipeIdentifier(identifier: ResourceLocation): ResourceLocation = identifier

  override def createRecipeProvider(provider: HolderLookup.Provider, recipeOutput: RecipeOutput): RecipeProvider = {
    val ip = new IngredientProviderFabric((o, c) => this.withConditions(o, c *), provider.lookupOrThrow(Registries.ITEM))

    given p: HolderLookup.Provider = provider

    given r: RecipeOutput = recipeOutput

    new Recipe(ip)
  }

  override def getName: String = getClass.getSimpleName
}
