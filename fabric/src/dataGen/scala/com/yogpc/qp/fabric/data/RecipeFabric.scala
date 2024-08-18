package com.yogpc.qp.fabric.data

import com.yogpc.qp.data.Recipe
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider
import net.minecraft.core.HolderLookup
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.resources.ResourceLocation

import java.util.concurrent.CompletableFuture

final class RecipeFabric(output: FabricDataOutput, registries: CompletableFuture[HolderLookup.Provider]) extends FabricRecipeProvider(output, registries) {
  private val ip = new IngredientProviderFabric((o, c) => this.withConditions(o, c *))
  private val internal = new Recipe(ip, output, registries)

  override def buildRecipes(exporter: RecipeOutput): Unit = internal.buildRecipes(exporter)

  override def getRecipeIdentifier(identifier: ResourceLocation): ResourceLocation = identifier
}
