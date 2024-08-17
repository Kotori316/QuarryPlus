package com.yogpc.qp.data

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.data.recipes.{ShapedRecipeBuilder, ShapelessRecipeBuilder}
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.level.ItemLike

extension (builder: ShapelessRecipeBuilder) {
  def unlockedBy(item: ItemLike): ShapelessRecipeBuilder = {
    val name = BuiltInRegistries.ITEM.getKey(item.asItem()).getPath
    builder.unlockedBy(s"has_$name", RecipeProviderAccess.hasItem(item))
    builder
  }

  def unlockedBy(tag: TagKey[Item]): ShapelessRecipeBuilder = {
    val name = tag.location().getPath
    builder.unlockedBy(s"has_$name", RecipeProviderAccess.hasTag(tag))
    builder
  }
}

extension (builder: ShapedRecipeBuilder) {
  def unlockedBy(item: ItemLike): ShapedRecipeBuilder = {
    val name = BuiltInRegistries.ITEM.getKey(item.asItem()).getPath
    builder.unlockedBy(s"has_$name", RecipeProviderAccess.hasItem(item))
    builder
  }

  def unlockedBy(tag: TagKey[Item]): ShapedRecipeBuilder = {
    val name = tag.location().getPath
    builder.unlockedBy(s"has_$name", RecipeProviderAccess.hasTag(tag))
    builder
  }
}
