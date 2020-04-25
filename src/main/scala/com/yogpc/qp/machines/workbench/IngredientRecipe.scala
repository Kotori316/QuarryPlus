package com.yogpc.qp.machines.workbench

import cats.data.{NonEmptyList, Validated, ValidatedNel}
import cats.implicits._
import cats.{Functor, Semigroupal}
import com.google.gson.{JsonObject, JsonParseException}
import com.yogpc.qp.machines.base.APowerTile
import com.yogpc.qp.machines.workbench.WorkbenchRecipes.LOGGER
import com.yogpc.qp.utils.ItemElement
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketBuffer
import net.minecraft.util.{JSONUtils, ResourceLocation}
import net.minecraftforge.common.crafting.CraftingHelper

import scala.jdk.CollectionConverters._

final class IngredientRecipe(location: ResourceLocation, o: ItemStack, e: Long, s: Boolean, seq: Seq[Seq[IngredientWithCount]])
  extends WorkbenchRecipes(location, ItemElement(o), e, s) {
  WorkbenchRecipes.LOGGER.debug("Recipe instance({}) created for {}. Input: {}", location, output, inputs)
  override val size: Int = seq.size

  override def inputs: Seq[Seq[IngredientWithCount]] = seq

  override def getOutput: ItemStack = o.copy()

  override val subTypeName: String = IngredientRecipe.subTypeName
}

object IngredientRecipe {
  final val subTypeName = "default"
  type EOR[A] = ValidatedNel[String, A]
  implicit val ff: Functor[EOR] with Semigroupal[EOR] = new Functor[EOR] with Semigroupal[EOR] {
    override def map[A, B](fa: EOR[A])(f: A => B): EOR[B] = fa map f

    override def product[A, B](fa: EOR[A], fb: EOR[B]): EOR[(A, B)] = fa product fb
  }
  val createIngredientRecipe: (JsonObject, ResourceLocation) => Either[String, WorkbenchRecipes] = { (json: JsonObject, location: ResourceLocation) =>
    val energy: EOR[Double] = Validated.catchNonFatal(JSONUtils.getString(json, "energy", "1000").toDouble)
      .leftMap(e => NonEmptyList.of(e.toString))
      .andThen(d => Validated.condNel(d > 0, d * APowerTile.MJToMicroMJ, "Energy must be over than 0"))
    val item: EOR[ItemStack] = Validated.catchNonFatal(CraftingHelper.getItemStack(JSONUtils.getJsonObject(json, "result"), true))
      .leftMap {
        case jsonEx: JsonParseException => NonEmptyList.of(jsonEx.getMessage)
        case ex => NonEmptyList.of(ex.toString)
      }
      .andThen(i => Validated.condNel(!i.isEmpty, i, "Result item is empty"))
    val seq: EOR[Seq[Seq[IngredientWithCount]]] = Validated.catchNonFatal(JSONUtils.getJsonArray(json, "ingredients").asScala.map(IngredientWithCount.getSeq).toSeq)
      .leftMap(e => NonEmptyList.of(e.toString))
    val value = (energy, item, seq).mapN { case (e, stack, recipe) =>
      val showInJei = JSONUtils.getBoolean(json, "showInJEI", true)
      val id = JSONUtils.getString(json, "id", "").some.filter(_.nonEmpty).map(new ResourceLocation(_)).getOrElse(location)
      new IngredientRecipe(id, stack, e.toLong, showInJei, recipe)
    }
    value.leftMap(_.mkString_(", ")).toEither
  }

  val packetSerialize: WorkbenchRecipes.PacketSerialize = new WorkbenchRecipes.PacketSerialize {
    override def read(recipeId: ResourceLocation, buffer: PacketBuffer): WorkbenchRecipes = {
      val location = buffer.readResourceLocation()
      LOGGER.debug("Serializer loaded {} created from packet.", location)
      val output = buffer.readItemStack()
      val energy = buffer.readLong()
      val showInJEI = buffer.readBoolean()

      val recipeSize = buffer.readVarInt()
      val builder = Seq.newBuilder[Seq[IngredientWithCount]]
      for (_ <- 0 until recipeSize) {
        val b2 = Seq.newBuilder[IngredientWithCount]
        val size = buffer.readVarInt()
        for (_ <- 0 until size) {
          b2 += IngredientWithCount.readFromBuffer(buffer)
        }
        builder += b2.result()
      }
      new IngredientRecipe(location, output, energy, showInJEI, builder.result())
    }

    override def write(buffer: PacketBuffer, recipe: WorkbenchRecipes): Unit = {
      buffer.writeResourceLocation(recipe.location)
      buffer.writeItemStack(recipe.getOutput)
      buffer.writeLong(recipe.energy)
      buffer.writeBoolean(recipe.showInJEI)

      buffer.writeVarInt(recipe.size)
      recipe.inputs.foreach { s =>
        buffer.writeVarInt(s.size)
        s.foreach(_.writeToBuffer(buffer))
      }
    }
  }
}