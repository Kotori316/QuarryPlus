package com.yogpc.qp.data

import com.google.gson.JsonObject
import com.yogpc.qp.data.QuarryPlusDataProvider.DataBuilder
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger
import net.minecraft.data.{IFinishedRecipe, ShapedRecipeBuilder, ShapelessRecipeBuilder}
import net.minecraft.tags.Tag
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.crafting.conditions.{ICondition, NotCondition, TagEmptyCondition}

case class RecipeSerializeHelper(recipe: IFinishedRecipe,
                                 conditions: List[ICondition] = Nil,
                                 saveName: ResourceLocation = null) extends DataBuilder {
  def this(c: ShapedRecipeBuilder, saveName: ResourceLocation) = {
    this(RecipeSerializeHelper.getConsumeValue(c), saveName = saveName)
  }

  def this(c: ShapelessRecipeBuilder, saveName: ResourceLocation) = {
    this(RecipeSerializeHelper.getConsumeValue(c), saveName = saveName)
  }

  def addCondition(condition: ICondition): RecipeSerializeHelper =
    copy(conditions = condition :: this.conditions)

  def addTagCondition(tag: Tag[_]): RecipeSerializeHelper =
    addCondition(new NotCondition(new TagEmptyCondition(tag.getId)))

  override def build: JsonObject = {
    val o = recipe.getRecipeJson
    if (conditions.nonEmpty)
      o.add("conditions", SerializeUtils.makeConditionArray(conditions))
    o
  }

  override def location = if (saveName == null) recipe.getID else saveName

}

object RecipeSerializeHelper {
  def by(c: ShapedRecipeBuilder, saveName: ResourceLocation): RecipeSerializeHelper = new RecipeSerializeHelper(c, saveName)

  def by(c: ShapelessRecipeBuilder, saveName: ResourceLocation): RecipeSerializeHelper = new RecipeSerializeHelper(c, saveName)

  private[this] final val dummyTrigger = new RecipeUnlockedTrigger.Instance(new ResourceLocation("dummy:dummy"))

  //noinspection DuplicatedCode
  private def getConsumeValue(c: ShapedRecipeBuilder): IFinishedRecipe = {
    c.addCriterion("dummy", dummyTrigger)
    var t: IFinishedRecipe = null
    c.build(p => t = p)
    t
  }

  //noinspection DuplicatedCode
  private def getConsumeValue(c: ShapelessRecipeBuilder): IFinishedRecipe = {
    c.addCriterion("dummy", dummyTrigger)
    var t: IFinishedRecipe = null
    c.build(p => t = p)
    t
  }

}
