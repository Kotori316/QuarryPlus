package com.yogpc.qp.data

import com.yogpc.qp.QuarryPlus
import net.minecraft.core.registries.Registries
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.crafting.Ingredient

trait IngredientProvider {
  def glowStoneDust: Ingredient

  def redStoneDust: Ingredient

  def lapis: Ingredient

  def diamond: Ingredient

  def amethyst: Ingredient

  def ironIngot: Ingredient

  def goldIngot: Ingredient

  def enderPearl: Ingredient

  def netherStar: Ingredient

  def obsidian: Ingredient = Ingredient.of(obsidianTag)

  def obsidianTag: TagKey[Item]

  def glass: Ingredient

  def redStoneBlock: Ingredient

  def goldBlock: Ingredient

  def diamondBlock: Ingredient

  def emeraldBlock: Ingredient

  def pickaxeForQuarry: Ingredient

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

  def repeatTickModuleRecipeOutput(original: RecipeOutput): RecipeOutput = original

  def filterModuleRecipeOutput(original: RecipeOutput): RecipeOutput = original
}
