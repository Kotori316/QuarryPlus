package com.yogpc.qp.forge.data

import com.google.gson.{JsonElement, JsonObject}
import com.mojang.serialization.{DynamicOps, JsonOps}
import com.yogpc.qp.data.Recipe
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.Registries
import net.minecraft.data.{CachedOutput, DataProvider, PackOutput}
import net.minecraftforge.common.crafting.conditions.{AndCondition, ICondition}

import java.util.concurrent.CompletableFuture
import scala.jdk.javaapi.CollectionConverters

final class RecipeForge(output: PackOutput, registries: CompletableFuture[HolderLookup.Provider]) extends DataProvider {
  private final val ip = IngredientProviderForge()
  private final val internal = Recipe(ip, output, registries)

  override def run(pOutput: CachedOutput): CompletableFuture[?] = {
    val recipePathProvider = output.createRegistryElementsPathProvider(Registries.RECIPE)
    val advancementPathProvider = output.createRegistryElementsPathProvider(Registries.ADVANCEMENT)

    registries.thenApplyAsync[CollectRecipe] { provider =>
      val collector = CollectRecipe(provider)
      internal.buildRecipes(collector)
      collector
    }.thenApplyAsync { collected =>
      val registryOps = collected.registry().createSerializationContext(JsonOps.INSTANCE)
      val recipeFeatures = collected.getSavedRecipes.map { (id, recipe, conditions) =>
        val json = net.minecraft.world.item.crafting.Recipe.CODEC.encodeStart(registryOps, recipe).getOrThrow().getAsJsonObject
        saveConditions(registryOps, conditions, json)
        val path = recipePathProvider.json(id)
        DataProvider.saveStable(pOutput, json, path)
      }

      val advancementFeatures = collected.getSavedAdvancements.map { (id, advancement, conditions) =>
        val json = advancement.getAsJsonObject
        saveConditions(registryOps, conditions, json)
        val path = advancementPathProvider.json(id)
        DataProvider.saveStable(pOutput, json, path)
      }

      CompletableFuture.allOf(recipeFeatures ++ advancementFeatures *)
    }
  }

  override def getName: String = getClass.getSimpleName

  private def saveConditions(registryOps: DynamicOps[JsonElement], conditions: Seq[ICondition], json: JsonObject): Unit = {
    if (conditions.nonEmpty) {
      val singleCondition = new AndCondition(CollectionConverters.asJava(conditions))
      val encoded = ICondition.CODEC.encodeStart(registryOps, singleCondition).getOrThrow()
      json.add(ICondition.DEFAULT_FIELD, encoded)
    }
  }
}
