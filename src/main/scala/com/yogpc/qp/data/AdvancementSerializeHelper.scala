package com.yogpc.qp.data

import com.google.gson.JsonObject
import com.yogpc.qp.data.QuarryPlusDataProvider.DataBuilder
import net.minecraft.advancements.criterion._
import net.minecraft.advancements.{Advancement, AdvancementRewards, ICriterionInstance, IRequirementsStrategy}
import net.minecraft.item.Item
import net.minecraft.tags.{ITag, Tag}
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.crafting.conditions.{ICondition, NotCondition, TagEmptyCondition}

case class AdvancementSerializeHelper(name: ResourceLocation,
                                      criterionList: List[(String, ICriterionInstance)] = Nil,
                                      conditions: List[ICondition] = Nil,
                                      saveName: ResourceLocation = null) extends DataBuilder {

  def addCriterion(name: String, criterion: ICriterionInstance): AdvancementSerializeHelper =
    copy(criterionList = (name, criterion) :: criterionList)

  def addItemCriterion(item: Item): AdvancementSerializeHelper =
    addCriterion(s"has_${item.getRegistryName.getPath}", InventoryChangeTrigger.Instance.forItems(item))

  def addItemCriterion(tag: ITag.INamedTag[Item]): AdvancementSerializeHelper =
    addCriterion(s"has_${tag.func_230234_a_().getPath}", InventoryChangeTrigger.Instance.forItems(ItemPredicate.Builder.create().tag(tag).build()))
      .addCondition(new NotCondition(new TagEmptyCondition(tag.func_230234_a_())))

  def addCondition(condition: ICondition): AdvancementSerializeHelper =
    copy(conditions = condition :: conditions)

  override def build: JsonObject = {
    val builder = Advancement.Builder.builder()
    builder.withParentId(new ResourceLocation("recipes/root"))
      .withCriterion("has_the_recipe", RecipeUnlockedTrigger.func_235675_a_(name))
      .withRewards(AdvancementRewards.Builder.recipe(name))
      .withRequirementsStrategy(IRequirementsStrategy.OR)
    val obj = criterionList.foldRight(builder) { case ((s, c), b) => b.withCriterion(s, c) }
      .serialize()
    obj.add("conditions", SerializeUtils.makeConditionArray(conditions))
    obj
  }

  override def location: ResourceLocation = if (saveName == null) name else saveName
}
