package com.yogpc.qp.machines.workbench

import com.google.gson.{JsonArray, JsonElement, JsonObject}
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.Ingredient
import net.minecraft.network.PacketBuffer
import net.minecraft.util.JSONUtils
import net.minecraftforge.common.crafting.CraftingHelper

import scala.collection.immutable.ArraySeq
import scala.util.Try

case class IngredientWithCount(ingredient: Ingredient, count: Int) {

  def this(json: JsonObject) = {
    this(CraftingHelper.getIngredient(json), JSONUtils.getInt(json, "count"))
  }

  def this(stack: ItemStack) = {
    this(Ingredient.fromStacks(stack), stack.getCount)
  }

  def matches(stack: ItemStack): Boolean = {
    ingredient.test(stack) && stack.getCount >= count
  }

  def stackList: Seq[ItemStack] = {
    ArraySeq.unsafeWrapArray(ingredient.getMatchingStacks)
      .map(_.copy()).filter(s => !s.isEmpty).map { s =>
      s.setCount(count)
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
    Try(ingredient.getMatchingStacks).toOption.flatMap(_.headOption) match {
      case Some(stack) => s"${stack.getItem} x$count}"
      case None => "Empty"
    }
  }

  def writeToBuffer(buffer: PacketBuffer): Unit = {
    ingredient.write(buffer)
    buffer.writeInt(count)
  }

  def serializeJson: JsonElement = {
    val obj = ingredient.serialize()
    obj match {
      case array: JsonArray => array.forEach(e => e.getAsJsonObject.addProperty("count", count))
      case jsonObject: JsonObject => jsonObject.addProperty("count", count)
    }
    obj
  }
}

object IngredientWithCount {
  private final val changer: JsonElement => JsonObject = _.getAsJsonObject

  def getSeq(json: JsonElement): Seq[IngredientWithCount] = {
    val factory: JsonObject => IngredientWithCount = new IngredientWithCount(_)
    if (json.isJsonArray) {
      import scala.jdk.CollectionConverters._
      json.getAsJsonArray.asScala.map(changer andThen factory).toSeq
    } else {
      Seq(factory(json.getAsJsonObject))
    }
  }

  def getSeq(stack: ItemStack): Seq[IngredientWithCount] = {
    Seq(new IngredientWithCount(stack))
  }

  def readFromBuffer(buffer: PacketBuffer): IngredientWithCount = {
    val ingredient = Ingredient.read(buffer)
    val count = buffer.readInt()
    IngredientWithCount(ingredient, count)
  }
}
