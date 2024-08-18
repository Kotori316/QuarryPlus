package com.yogpc.qp.forge.data

import com.google.gson.JsonElement
import net.minecraft.advancements.Advancement
import net.minecraft.core.HolderLookup
import net.minecraft.data.recipes.{RecipeBuilder, RecipeOutput}
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.Recipe
import net.minecraftforge.common.crafting.conditions.ICondition

class CollectRecipe(r: HolderLookup.Provider) extends RecipeOutput {
  private final val recipes: scala.collection.mutable.ArrayBuffer[(ResourceLocation, Recipe[?], Seq[ICondition])] = scala.collection.mutable.ArrayBuffer.empty
  private final val advancements: scala.collection.mutable.ArrayBuffer[(ResourceLocation, JsonElement, Seq[ICondition])] = scala.collection.mutable.ArrayBuffer.empty

  //noinspection ScalaDeprecation
  override def advancement(): Advancement.Builder = Advancement.Builder.recipeAdvancement.parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT)

  override def accept(id: ResourceLocation, recipe: Recipe[?], advancementId: ResourceLocation, advancement: JsonElement): Unit = {
    recipes.addOne((id, recipe, Seq.empty))
    advancements.addOne((advancementId, advancement, Seq.empty))
  }

  override def registry(): HolderLookup.Provider = r

  def getSavedRecipes: Seq[(ResourceLocation, Recipe[?], Seq[ICondition])] = recipes.toSeq

  def getSavedAdvancements: Seq[(ResourceLocation, JsonElement, Seq[ICondition])] = advancements.toSeq

  def withCondition(conditions: Seq[ICondition]): RecipeOutput = WithCondition(conditions)

  private final class WithCondition(conditions: Seq[ICondition]) extends RecipeOutput {

    override def accept(id: ResourceLocation, recipe: Recipe[?], advancementId: ResourceLocation, advancement: JsonElement): Unit = {
      CollectRecipe.this.recipes.addOne((id, recipe, conditions))
      CollectRecipe.this.advancements.addOne((advancementId, advancement, conditions))
    }

    override def registry(): HolderLookup.Provider = CollectRecipe.this.registry()

    override def advancement(): Advancement.Builder = CollectRecipe.this.advancement()
  }
}
