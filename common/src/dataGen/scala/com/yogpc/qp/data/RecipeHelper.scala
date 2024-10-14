package com.yogpc.qp.data

import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.data.recipes.{RecipeBuilder, RecipeOutput}
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.level.ItemLike

extension [T <: RecipeBuilder](builder: T) {
  def unlockedBy(item: ItemLike)(using provider: HolderLookup.Provider, recipeOutput: RecipeOutput): T = {
    val name = BuiltInRegistries.ITEM.getKey(item.asItem()).getPath
    builder.unlockedBy(s"has_$name", RecipeProviderAccess.hasItem(item, RecipeProviderAccess(provider, recipeOutput)))
    builder
  }

  def unlockedBy(tag: TagKey[Item])(using provider: HolderLookup.Provider, recipeOutput: RecipeOutput): T = {
    val name = tag.location().getPath
    builder.unlockedBy(s"has_$name", RecipeProviderAccess.hasTag(tag, RecipeProviderAccess(provider, recipeOutput)))
    builder
  }
}
