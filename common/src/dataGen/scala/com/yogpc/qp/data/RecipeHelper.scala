package com.yogpc.qp.data

import net.minecraft.advancements.Advancement
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.data.recipes.RecipeBuilder
import net.minecraft.data.worldgen.BootstrapContext
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.crafting.Recipe as McRecipe
import net.minecraft.world.level.ItemLike

extension [T <: RecipeBuilder](builder: T) {
  def unlockedBy(item: ItemLike)(using recipeContext: BootstrapContext[McRecipe[?]], advancementContext: BootstrapContext[Advancement]): T = {
    val name = BuiltInRegistries.ITEM.getKey(item.asItem()).getPath
    builder.unlockedBy(s"has_$name", RecipeProviderAccess.hasItem(item, RecipeProviderAccess(recipeContext, advancementContext)))
    builder
  }

  def unlockedBy(tag: TagKey[Item])(using recipeContext: BootstrapContext[McRecipe[?]], advancementContext: BootstrapContext[Advancement]): T = {
    val name = tag.location().getPath
    builder.unlockedBy(s"has_$name", RecipeProviderAccess.hasTag(tag, RecipeProviderAccess(recipeContext, advancementContext)))
    builder
  }
}
