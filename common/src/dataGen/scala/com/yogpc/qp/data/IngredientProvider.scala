package com.yogpc.qp.data

import com.yogpc.qp.QuarryPlus
import net.minecraft.core.registries.Registries
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.{Item, Items}

trait IngredientProvider {
  def glowStoneDust: Ingredient

  def redStoneDust: Ingredient

  def lapis: Ingredient

  def diamond: Ingredient

  def ironIngot: Ingredient

  def goldIngot: Ingredient

  def enderPearl: Ingredient

  def obsidian: Ingredient

  def glass: Ingredient

  def redStoneBlock: Ingredient

  def goldBlock: Ingredient

  def diamondBlock: Ingredient

  def pickaxeForQuarry: Ingredient = Ingredient.of(Items.GOLDEN_PICKAXE)

  def marker: Ingredient = {
    val tag = markerTag
    Ingredient.of(tag)
  }

  def markerTag: TagKey[Item] = {
    TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, "markers"))
  }

  def pumpModuleRecipeOutput(original: RecipeOutput): RecipeOutput = original

  def expModuleRecipeOutput(original: RecipeOutput): RecipeOutput = original

  def installBedrockModuleQuarryRecipeOutput(original: RecipeOutput): RecipeOutput = original
}
