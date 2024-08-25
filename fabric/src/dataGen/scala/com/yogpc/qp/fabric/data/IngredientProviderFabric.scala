package com.yogpc.qp.fabric.data

import com.yogpc.qp.data.IngredientProvider
import net.fabricmc.fabric.api.resource.conditions.v1.{ResourceCondition, ResourceConditions}
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.Ingredient

final class IngredientProviderFabric(withCondition: (RecipeOutput, Seq[ResourceCondition]) => RecipeOutput) extends IngredientProvider {
  override def glowStoneDust: Ingredient = Ingredient.of(ConventionalItemTags.GLOWSTONE_DUSTS)

  override def redStoneDust: Ingredient = Ingredient.of(ConventionalItemTags.REDSTONE_DUSTS)

  override def lapis: Ingredient = Ingredient.of(ConventionalItemTags.LAPIS_GEMS)

  override def diamond: Ingredient = Ingredient.of(ConventionalItemTags.DIAMOND_GEMS)

  override def ironIngot: Ingredient = Ingredient.of(ConventionalItemTags.IRON_INGOTS)

  override def goldIngot: Ingredient = Ingredient.of(ConventionalItemTags.GOLD_INGOTS)

  override def obsidian: Ingredient = Ingredient.of(Items.OBSIDIAN)

  override def glass: Ingredient = Ingredient.of(ConventionalItemTags.GLASS_BLOCKS)

  override def redStoneBlock: Ingredient = Ingredient.of(ConventionalItemTags.STORAGE_BLOCKS_REDSTONE)

  override def goldBlock: Ingredient = Ingredient.of(ConventionalItemTags.STORAGE_BLOCKS_GOLD)

  override def diamondBlock: Ingredient = Ingredient.of(ConventionalItemTags.STORAGE_BLOCKS_DIAMOND)

  override def pumpModuleRecipeOutput(original: RecipeOutput): RecipeOutput = {
    val condition = ResourceConditions.not(ResourceConditions.alwaysTrue())
    withCondition(original, Seq(condition))
  }

  override def expModuleRecipeOutput(original: RecipeOutput): RecipeOutput = pumpModuleRecipeOutput(original)

  override def installBedrockModuleQuarryRecipeOutput(original: RecipeOutput): RecipeOutput = pumpModuleRecipeOutput(original)

  override def enderPearl: Ingredient = Ingredient.of(ConventionalItemTags.ENDER_PEARLS)
}
