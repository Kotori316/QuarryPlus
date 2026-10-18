package com.yogpc.qp.forge.data

import com.yogpc.qp.data.IngredientProvider
import net.minecraft.core.HolderGetter
import net.minecraft.data.recipes.{RecipeCategory, RecipeOutput}
import net.minecraft.tags.TagKey
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.{Item, Items}
import net.minecraftforge.common.Tags
import net.minecraftforge.common.crafting.conditions.FalseCondition

final class IngredientProviderForge(itemRegistry: HolderGetter[Item]) extends IngredientProvider(itemRegistry) {

  override def glowStoneDust: Ingredient = Ingredient.of(itemRegistry.getOrThrow(Tags.Items.DUSTS_GLOWSTONE))

  override def redStoneDust: Ingredient = Ingredient.of(itemRegistry.getOrThrow(Tags.Items.DUSTS_REDSTONE))

  override def lapis: Ingredient = Ingredient.of(itemRegistry.getOrThrow(Tags.Items.GEMS_LAPIS))

  override def diamond: Ingredient = Ingredient.of(itemRegistry.getOrThrow(Tags.Items.GEMS_DIAMOND))

  override def ironIngot: Ingredient = Ingredient.of(itemRegistry.getOrThrow(Tags.Items.INGOTS_IRON))

  override def goldIngot: Ingredient = Ingredient.of(itemRegistry.getOrThrow(Tags.Items.INGOTS_GOLD))

  override def obsidianTag: TagKey[Item] = Tags.Items.OBSIDIANS

  override def glass: Ingredient = Ingredient.of(itemRegistry.getOrThrow(Tags.Items.GLASS_BLOCKS))

  override def redStoneBlock: Ingredient = Ingredient.of(itemRegistry.getOrThrow(Tags.Items.STORAGE_BLOCKS_REDSTONE))

  override def goldBlock: Ingredient = Ingredient.of(itemRegistry.getOrThrow(Tags.Items.STORAGE_BLOCKS_GOLD))

  override def diamondBlock: Ingredient = Ingredient.of(itemRegistry.getOrThrow(Tags.Items.STORAGE_BLOCKS_DIAMOND))

  override def pickaxeForQuarry: Ingredient = Ingredient.of(Items.DIAMOND_PICKAXE)

  override def installBedrockModuleQuarryRecipeOutput(original: RecipeOutput): RecipeOutput = {
    val conditionalRecipeOutput = new ConditionalRecipeOutput(original)
    conditionalRecipeOutput.conditionalRecipeBuilder.condition(FalseCondition.INSTANCE)
    conditionalRecipeOutput.conditionalRecipeBuilder.category(RecipeCategory.MISC)
    conditionalRecipeOutput
  }

  override def enderPearl: Ingredient = Ingredient.of(itemRegistry.getOrThrow(Tags.Items.ENDER_PEARLS))

  override def amethyst: Ingredient = Ingredient.of(itemRegistry.getOrThrow(Tags.Items.GEMS_AMETHYST))

  override def netherStar: Ingredient = Ingredient.of(itemRegistry.getOrThrow(Tags.Items.NETHER_STARS))

  override def emeraldBlock: Ingredient = Ingredient.of(itemRegistry.getOrThrow(Tags.Items.STORAGE_BLOCKS_EMERALD))
}
