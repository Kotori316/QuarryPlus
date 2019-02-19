package com.yogpc.qp.utils

import com.google.gson.{JsonElement, JsonObject}
import com.yogpc.qp.version.VersionUtil
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.Ingredient
import net.minecraft.util.JsonUtils
import net.minecraftforge.common.crafting.{CraftingHelper, JsonContext}

case class IngredientWithCount(ingredient: Ingredient, count: Int) {

  def this(json: JsonObject, ctx: JsonContext) = {
    this(CraftingHelper.getIngredient(json, ctx), JsonUtils.getInt(json, "count"))
  }

  def this(stack: ItemStack) = {
    this(Ingredient.fromStacks(stack), VersionUtil.getCount(stack))
  }

  def matches(stack: ItemStack): Boolean = {
    ingredient.apply(stack) && VersionUtil.getCount(stack) >= count
  }

  def stackList: Seq[ItemStack] = {
    ingredient.getMatchingStacks.map(_.copy()).filter(VersionUtil.nonEmpty).map { s =>
      VersionUtil.setCount(s, count)
      s
    }
  }

  def shrink(stack: ItemStack): Boolean = {
    if (!matches(stack))
      return false
    stack.shrink(count)
    true
  }

  override def toString: String = {
    ingredient.getMatchingStacks.headOption match {
      case Some(stack) => s"$stack x$count}"
      case None => "Empty"
    }
  }
}

object IngredientWithCount {
  private final val changer: JsonElement => JsonObject = _.getAsJsonObject

  def getSeq(json: JsonElement, ctx: JsonContext): Seq[IngredientWithCount] = {
    val factory: JsonObject => IngredientWithCount = new IngredientWithCount(_, ctx)
    if (json.isJsonArray) {
      import scala.collection.JavaConverters._
      json.getAsJsonArray.asScala.map(changer andThen factory).toSeq
    } else {
      Seq(factory(json.getAsJsonObject))
    }
  }

  def getSeq(stack: ItemStack): Seq[IngredientWithCount] = {
    Seq(new IngredientWithCount(stack))
  }
}