package com.yogpc.qp.forge.data

import com.yogpc.qp.data.IngredientProvider
import net.minecraft.world.item.crafting.Ingredient
import net.minecraftforge.common.Tags

final class IngredientProviderForge extends IngredientProvider {

  override def glowStoneDust: Ingredient = Ingredient.of(Tags.Items.DUSTS_GLOWSTONE)

  override def redStoneDust: Ingredient = Ingredient.of(Tags.Items.DUSTS_REDSTONE)

  override def lapis: Ingredient = Ingredient.of(Tags.Items.GEMS_LAPIS)

  override def diamond: Ingredient = Ingredient.of(Tags.Items.GEMS_DIAMOND)

  override def ironIngot: Ingredient = Ingredient.of(Tags.Items.INGOTS_IRON)

  override def goldIngot: Ingredient = Ingredient.of(Tags.Items.INGOTS_GOLD)

  override def obsidian: Ingredient = Ingredient.of(Tags.Items.OBSIDIAN)

  override def glass: Ingredient = Ingredient.of(Tags.Items.GLASS)

  override def redStoneBlock: Ingredient = Ingredient.of(Tags.Items.STORAGE_BLOCKS_REDSTONE)

  override def goldBlock: Ingredient = Ingredient.of(Tags.Items.STORAGE_BLOCKS_GOLD)

  override def diamondBlock: Ingredient = Ingredient.of(Tags.Items.STORAGE_BLOCKS_DIAMOND)
}
