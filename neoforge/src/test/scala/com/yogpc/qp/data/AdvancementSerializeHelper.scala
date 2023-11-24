package com.yogpc.qp.data

import com.google.gson.{JsonElement, JsonObject}
import net.minecraft.advancements.critereon.{InventoryChangeTrigger, ItemPredicate, RecipeUnlockedTrigger}
import net.minecraft.advancements.{Advancement, AdvancementRequirements, AdvancementRewards}
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.data.recipes.RecipeBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.level.ItemLike
import net.neoforged.neoforge.common.conditions.{ICondition, NotCondition, TagEmptyCondition}

import scala.jdk.javaapi.CollectionConverters

case class AdvancementSerializeHelper private(location: ResourceLocation,
                                              builder: Advancement.Builder,
                                              conditions: List[ICondition])
  extends DataBuilder {

  override def build(provider: HolderLookup.Provider): JsonElement = {
    val obj: JsonObject = builder.save(h => {}, "").value().serializeToJson()
    if (conditions.nonEmpty) {
      ICondition.writeConditions(provider, obj, CollectionConverters.asJava(conditions))
    }
    obj
  }

  def addItemCriterion(item: ItemLike): AdvancementSerializeHelper = {
    val name: String = BuiltInRegistries.ITEM.getKey(item.asItem()).getPath
    this.copy(builder = builder.addCriterion(s"has_$name", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item.of(item).build)))
  }

  def addTagCriterion(tag: TagKey[Item]): AdvancementSerializeHelper = {
    val name = tag.location.getPath
    this.copy(
      builder = builder.addCriterion(s"has_$name", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item.of(tag).build)),
      conditions = new NotCondition(new TagEmptyCondition(tag)) :: conditions)
  }

  def addCondition(condition: ICondition): AdvancementSerializeHelper = {
    this.copy(conditions = condition :: conditions)
  }
}

object AdvancementSerializeHelper {
  def apply(location: ResourceLocation): AdvancementSerializeHelper = apply(location, recipeLocation = location)

  //noinspection ScalaDeprecation,deprecation
  def apply(location: ResourceLocation, recipeLocation: ResourceLocation): AdvancementSerializeHelper = {
    val base = Advancement.Builder.recipeAdvancement()
      .parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT)
      .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(recipeLocation))
      .rewards(AdvancementRewards.Builder.recipe(recipeLocation))
      .requirements(AdvancementRequirements.Strategy.OR)
    new AdvancementSerializeHelper(location, base, List.empty)
  }
}
