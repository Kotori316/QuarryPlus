package com.yogpc.qp.forge.data

import net.minecraft.advancements.{Advancement, AdvancementHolder}
import net.minecraft.core.{Holder, HolderGetter, HolderLookup, Registry}
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.crafting.Recipe
import net.minecraftforge.common.crafting.ConditionalRecipe

import java.util.{Optional, stream}

class ConditionalRecipeOutput(private val original: RecipeOutput) extends RecipeOutput {
  val conditionalRecipeBuilder: ConditionalRecipe.Builder = ConditionalRecipe.builder(original)
  private val wrappedRecipeOutput: RecipeOutput = {
    var o: RecipeOutput = null
    conditionalRecipeBuilder.recipe(t => o = t)
    o
  }

  override def accept(id: ResourceKey[Recipe[?]], recipe: Recipe[?], advancement: AdvancementHolder): Unit = {
    wrappedRecipeOutput.accept(id, recipe, advancement)
    conditionalRecipeBuilder.save(original, id.identifier())
  }

  override def advancement(): Advancement.Builder = wrappedRecipeOutput.advancement()

  override def lookup[S](key: ResourceKey[? <: Registry[? <: S]]): HolderGetter[S] = wrappedRecipeOutput.lookup(key)

  //noinspection ScalaDeprecation
  @deprecated
  override def listContextElements[S](key: ResourceKey[? <: Registry[? <: S]]): stream.Stream[Holder.Reference[S]] = wrappedRecipeOutput.listContextElements(key)

  override def registryLookup[S](registry: ResourceKey[? <: Registry[? <: S]]): Optional[HolderLookup.RegistryLookup[S]] = wrappedRecipeOutput.registryLookup(registry)
}
