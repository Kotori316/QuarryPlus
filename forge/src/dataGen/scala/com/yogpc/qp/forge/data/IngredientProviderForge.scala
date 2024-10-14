package com.yogpc.qp.forge.data

import com.yogpc.qp.data.IngredientProvider
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.tags.TagKey
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.{Item, Items}
import net.minecraftforge.common.Tags
import net.minecraftforge.common.crafting.conditions.FalseCondition

final class IngredientProviderForge extends IngredientProvider {

  override def glowStoneDust: Ingredient = Ingredient.of(Tags.Items.DUSTS_GLOWSTONE)

  override def redStoneDust: Ingredient = Ingredient.of(Tags.Items.DUSTS_REDSTONE)

  override def lapis: Ingredient = Ingredient.of(Tags.Items.GEMS_LAPIS)

  override def diamond: Ingredient = Ingredient.of(Tags.Items.GEMS_DIAMOND)

  override def ironIngot: Ingredient = Ingredient.of(Tags.Items.INGOTS_IRON)

  override def goldIngot: Ingredient = Ingredient.of(Tags.Items.INGOTS_GOLD)

  override def obsidianTag: TagKey[Item] = Tags.Items.OBSIDIANS

  override def glass: Ingredient = Ingredient.of(Tags.Items.GLASS_BLOCKS)

  override def redStoneBlock: Ingredient = Ingredient.of(Tags.Items.STORAGE_BLOCKS_REDSTONE)

  override def goldBlock: Ingredient = Ingredient.of(Tags.Items.STORAGE_BLOCKS_GOLD)

  override def diamondBlock: Ingredient = Ingredient.of(Tags.Items.STORAGE_BLOCKS_DIAMOND)

  override def installBedrockModuleQuarryRecipeOutput(original: RecipeOutput): RecipeOutput = {
    original match {
      case recipe: CollectRecipe => recipe.withCondition(Seq(FalseCondition.INSTANCE))
      case _ => original
    }
  }

  override def enderPearl: Ingredient = Ingredient.of(Tags.Items.ENDER_PEARLS)

  override def amethyst: Ingredient = Ingredient.of(Tags.Items.GEMS_AMETHYST)

  override def prismarineShard: Ingredient = Ingredient.of(Items.PRISMARINE_SHARD)

  override def netherStar: Ingredient = Ingredient.of(Tags.Items.NETHER_STARS)

  override def emeraldBlock: Ingredient = Ingredient.of(Tags.Items.STORAGE_BLOCKS_EMERALD)
}
