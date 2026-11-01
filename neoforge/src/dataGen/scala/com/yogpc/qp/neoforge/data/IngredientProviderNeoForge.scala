package com.yogpc.qp.neoforge.data

import com.yogpc.qp.data.IngredientProvider
import net.minecraft.core.HolderGetter
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.tags.TagKey
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.{Item, Items}
import net.neoforged.neoforge.common.Tags
import net.neoforged.neoforge.common.conditions.NeverCondition

final class IngredientProviderNeoForge(itemRegistry: HolderGetter[Item]) extends IngredientProvider(itemRegistry) {
  override def glowStoneDust: Ingredient = Ingredient.of(itemRegistry.getOrThrow(Tags.Items.DUSTS_GLOWSTONE))

  override def lapis: Ingredient = Ingredient.of(itemRegistry.getOrThrow(Tags.Items.GEMS_LAPIS))

  override def diamond: Ingredient = Ingredient.of(itemRegistry.getOrThrow(Tags.Items.GEMS_DIAMOND))

  override def ironIngot: Ingredient = Ingredient.of(itemRegistry.getOrThrow(Tags.Items.INGOTS_IRON))

  override def goldIngot: Ingredient = Ingredient.of(itemRegistry.getOrThrow(Tags.Items.INGOTS_GOLD))

  override def obsidianTag: TagKey[Item] = Tags.BlockItems.OBSIDIANS.item()

  override def glass: Ingredient = Ingredient.of(itemRegistry.getOrThrow(Tags.BlockItems.GLASS_BLOCKS.item()))

  override def redStoneBlock: Ingredient = Ingredient.of(itemRegistry.getOrThrow(Tags.BlockItems.STORAGE_BLOCKS_REDSTONE.item()))

  override def pickaxeForQuarry: Ingredient = Ingredient.of(Items.DIAMOND_PICKAXE)

  override def redStoneDust: Ingredient = Ingredient.of(itemRegistry.getOrThrow(Tags.Items.DUSTS_REDSTONE))

  override def goldBlock: Ingredient = Ingredient.of(itemRegistry.getOrThrow(Tags.BlockItems.STORAGE_BLOCKS_GOLD.item()))

  override def diamondBlock: Ingredient = Ingredient.of(itemRegistry.getOrThrow(Tags.BlockItems.STORAGE_BLOCKS_DIAMOND.item()))

  override def installBedrockModuleQuarryRecipeOutput(original: RecipeOutput): RecipeOutput = {
    original.withConditions(NeverCondition.INSTANCE)
  }

  override def enderPearl: Ingredient = Ingredient.of(itemRegistry.getOrThrow(Tags.Items.ENDER_PEARLS))

  override def amethyst: Ingredient = Ingredient.of(itemRegistry.getOrThrow(Tags.Items.GEMS_AMETHYST))

  override def netherStar: Ingredient = Ingredient.of(itemRegistry.getOrThrow(Tags.Items.NETHER_STARS))

  override def emeraldBlock: Ingredient = Ingredient.of(itemRegistry.getOrThrow(Tags.BlockItems.STORAGE_BLOCKS_EMERALD.item()))
}
