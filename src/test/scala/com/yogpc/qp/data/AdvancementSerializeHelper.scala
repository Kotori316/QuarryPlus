package com.yogpc.qp.data

import com.google.gson.{JsonArray, JsonElement, JsonObject}
import net.minecraft.advancements.critereon.{InventoryChangeTrigger, ItemPredicate, RecipeUnlockedTrigger}
import net.minecraft.advancements.{Advancement, AdvancementRewards, RequirementsStrategy}
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.level.ItemLike
import net.minecraftforge.common.crafting.CraftingHelper
import net.minecraftforge.common.crafting.conditions.{ICondition, NotCondition, TagEmptyCondition}
import net.minecraftforge.registries.ForgeRegistries

case class AdvancementSerializeHelper private(location: ResourceLocation,
                                              builder: Advancement.Builder,
                                              conditions: List[ICondition])
  extends DataBuilder {

  override def build(): JsonElement = {
    val obj: JsonObject = builder.serializeToJson()
    if (conditions.nonEmpty) {
      val conditionArray: JsonArray = conditions.map(CraftingHelper.serialize).foldLeft(new JsonArray()) {
        case (a, o) => a.add(o); a
      }
      obj.add("conditions", conditionArray)
    }
    obj
  }

  def addItemCriterion(item: ItemLike): AdvancementSerializeHelper = {
    val name: String = ForgeRegistries.ITEMS.getKey(item.asItem()).getPath
    this.copy(builder = builder.addCriterion(s"has_$name", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item.of(item).build)))
  }

  def addTagCriterion(tag: TagKey[Item]): AdvancementSerializeHelper = {
    val name = tag.location.getPath
    this.copy(
      builder = builder.addCriterion(s"has_$name", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item.of(tag).build)),
      conditions = new NotCondition(new TagEmptyCondition(tag.location)) :: conditions)
  }

  def addCondition(condition: ICondition): AdvancementSerializeHelper = {
    this.copy(conditions = condition :: conditions)
  }
}

object AdvancementSerializeHelper {
  def apply(location: ResourceLocation): AdvancementSerializeHelper = apply(location, recipeLocation = location)

  def apply(location: ResourceLocation, recipeLocation: ResourceLocation): AdvancementSerializeHelper = {
    val base = Advancement.Builder.advancement()
      .parent(new ResourceLocation("recipes/root"))
      .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(recipeLocation))
      .rewards(AdvancementRewards.Builder.recipe(recipeLocation))
      .requirements(RequirementsStrategy.OR)
    new AdvancementSerializeHelper(location, base, List.empty)
  }
}
