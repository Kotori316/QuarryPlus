package com.yogpc.qp.neoforge.data

import com.yogpc.qp.data.IngredientProvider
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.Ingredient
import net.neoforged.neoforge.common.Tags

final class IngredientProviderNeoForge extends IngredientProvider {
  override def glowStoneDust: Ingredient = Ingredient.of(Tags.Items.DUSTS_GLOWSTONE)

  override def lapis: Ingredient = Ingredient.of(Tags.Items.GEMS_LAPIS)

  override def diamond: Ingredient = Ingredient.of(Tags.Items.GEMS_DIAMOND)

  override def ironIngot: Ingredient = Ingredient.of(Tags.Items.INGOTS_IRON)

  override def goldIngot: Ingredient = Ingredient.of(Tags.Items.INGOTS_GOLD)

  override def obsidian: Ingredient = Ingredient.of(Tags.Items.OBSIDIANS)

  override def glass: Ingredient = Ingredient.of(Tags.Items.GLASS_BLOCKS)

  override def redStoneBlock: Ingredient = Ingredient.of(Tags.Items.STORAGE_BLOCKS_REDSTONE)

  override def pickaxeForQuarry: Ingredient = Ingredient.of(Items.GOLDEN_PICKAXE)

  override def redStoneDust: Ingredient = Ingredient.of(Tags.Items.DUSTS_REDSTONE)

  override def goldBlock: Ingredient = Ingredient.of(Tags.Items.STORAGE_BLOCKS_GOLD)

  override def diamondBlock: Ingredient = Ingredient.of(Tags.Items.STORAGE_BLOCKS_DIAMOND)
}
