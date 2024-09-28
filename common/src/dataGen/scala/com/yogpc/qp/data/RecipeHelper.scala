package com.yogpc.qp.data

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.data.recipes.RecipeBuilder
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.level.ItemLike

extension [T <: RecipeBuilder](builder: T) {
  def unlockedBy(item: ItemLike): T = {
    val name = BuiltInRegistries.ITEM.getKey(item.asItem()).getPath
    builder.unlockedBy(s"has_$name", RecipeProviderAccess.hasItem(item))
    builder
  }

  def unlockedBy(tag: TagKey[Item]): T = {
    val name = tag.location().getPath
    builder.unlockedBy(s"has_$name", RecipeProviderAccess.hasTag(tag))
    builder
  }
}
