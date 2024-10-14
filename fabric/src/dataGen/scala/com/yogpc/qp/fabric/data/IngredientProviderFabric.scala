package com.yogpc.qp.fabric.data

import com.yogpc.qp.data.IngredientProvider
import net.fabricmc.fabric.api.resource.conditions.v1.{ResourceCondition, ResourceConditions}
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags
import net.minecraft.core.HolderGetter
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.tags.TagKey
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.{Item, Items}

final class IngredientProviderFabric(withCondition: (RecipeOutput, Seq[ResourceCondition]) => RecipeOutput, itemRegistry: HolderGetter[Item]) extends IngredientProvider(itemRegistry) {
  override def glowStoneDust: Ingredient = Ingredient.of(itemRegistry.getOrThrow(ConventionalItemTags.GLOWSTONE_DUSTS))

  override def redStoneDust: Ingredient = Ingredient.of(itemRegistry.getOrThrow(ConventionalItemTags.REDSTONE_DUSTS))

  override def lapis: Ingredient = Ingredient.of(itemRegistry.getOrThrow(ConventionalItemTags.LAPIS_GEMS))

  override def diamond: Ingredient = Ingredient.of(itemRegistry.getOrThrow(ConventionalItemTags.DIAMOND_GEMS))

  override def ironIngot: Ingredient = Ingredient.of(itemRegistry.getOrThrow(ConventionalItemTags.IRON_INGOTS))

  override def goldIngot: Ingredient = Ingredient.of(itemRegistry.getOrThrow(ConventionalItemTags.GOLD_INGOTS))

  override def obsidianTag: TagKey[Item] = ConventionalItemTags.OBSIDIANS

  override def glass: Ingredient = Ingredient.of(itemRegistry.getOrThrow(ConventionalItemTags.GLASS_BLOCKS))

  override def redStoneBlock: Ingredient = Ingredient.of(itemRegistry.getOrThrow(ConventionalItemTags.STORAGE_BLOCKS_REDSTONE))

  override def goldBlock: Ingredient = Ingredient.of(itemRegistry.getOrThrow(ConventionalItemTags.STORAGE_BLOCKS_GOLD))

  override def diamondBlock: Ingredient = Ingredient.of(itemRegistry.getOrThrow(ConventionalItemTags.STORAGE_BLOCKS_DIAMOND))

  override def pickaxeForQuarry: Ingredient = Ingredient.of(Items.GOLDEN_PICKAXE)

  override def pumpModuleRecipeOutput(original: RecipeOutput): RecipeOutput = {
    val condition = ResourceConditions.not(ResourceConditions.alwaysTrue())
    withCondition(original, Seq(condition))
  }

  override def expModuleRecipeOutput(original: RecipeOutput): RecipeOutput = pumpModuleRecipeOutput(original)

  override def installBedrockModuleQuarryRecipeOutput(original: RecipeOutput): RecipeOutput = pumpModuleRecipeOutput(original)

  override def repeatTickModuleRecipeOutput(original: RecipeOutput): RecipeOutput = pumpModuleRecipeOutput(original)

  override def filterModuleRecipeOutput(original: RecipeOutput): RecipeOutput = pumpModuleRecipeOutput(original)

  override def enderPearl: Ingredient = Ingredient.of(itemRegistry.getOrThrow(ConventionalItemTags.ENDER_PEARLS))

  override def amethyst: Ingredient = Ingredient.of(itemRegistry.getOrThrow(ConventionalItemTags.AMETHYST_GEMS))

  override def netherStar: Ingredient = Ingredient.of(Items.NETHER_STAR)

  override def emeraldBlock: Ingredient = Ingredient.of(itemRegistry.getOrThrow(ConventionalItemTags.STORAGE_BLOCKS_EMERALD))
}
