package com.yogpc.qp.machines.workbench

import java.util

import cats.data.{NonEmptyList, Validated, ValidatedNel}
import cats.implicits._
import com.google.gson.{JsonArray, JsonObject}
import com.yogpc.qp.machines.base.APowerTile
import com.yogpc.qp.machines.workbench.WorkbenchRecipes.LOGGER
import com.yogpc.qp.utils.ItemElement
import net.minecraft.item.{ItemStack, Items}
import net.minecraft.nbt.CompoundNBT
import net.minecraft.network.PacketBuffer
import net.minecraft.util.{JSONUtils, ResourceLocation}
import net.minecraftforge.common.util.Constants.NBT

import scala.jdk.CollectionConverters._
import scala.util.chaining._

final class EnchantmentCopyRecipe(location: ResourceLocation, o: ItemStack, e: Long, copyFrom: Seq[IngredientWithCount], otherInput: Seq[Seq[IngredientWithCount]],
                                  enchantedBookMigration: Boolean = true)
  extends WorkbenchRecipes(location, ItemElement(o), e, showInJEI = true) {
  override val size: Int = 1 + otherInput.size

  override def inputs: Seq[Seq[IngredientWithCount]] = copyFrom +: otherInput

  override def getOutput(inputs: util.List[ItemStack]): ItemStack = {
    val stack = super.getOutput
    for {
      s <- inputs.asScala.find(i => copyFrom.exists(_.matches(i)))
      tag_raw <- Option(s.getTag).map(EnchantmentCopyRecipe.doEnchantedBookMigration(enchantedBookMigration, stack))
      tag = tag_raw.copy().tap(t => if (stack.hasTag) t.merge(stack.getTag))
    } {
      stack.setTag(tag)
    }
    stack
  }

  override def subTypeName: String = EnchantmentCopyRecipe.subName
}

object EnchantmentCopyRecipe {
  final val subName = "copy_enchantment"
  val createRecipe: (JsonObject, ResourceLocation) => Either[String, WorkbenchRecipes] = { (json, location) =>
    val energy: ValidatedNel[String, Long] = Validated.catchNonFatal(JSONUtils.getString(json, "energy", "1000").toDouble)
      .leftMap(e => NonEmptyList.of(e.toString))
      .andThen(d => Validated.condNel(d > 0, (d * APowerTile.MJToMicroMJ).toLong, "Energy must be over than 0"))
    val item: ValidatedNel[String, ItemStack] = IngredientRecipe.findItem(json, "result", "Result item is empty")
    val copyFrom: ValidatedNel[String, Seq[IngredientWithCount]] = Validated.catchNonFatal(IngredientWithCount.getSeq(json.get("enchantment_from")))
      .leftMap(_.toString).toValidatedNel
    val seq: ValidatedNel[String, Seq[Seq[IngredientWithCount]]] = Validated.catchNonFatal(
      JSONUtils.getJsonArray(json, "ingredients", new JsonArray).asScala.map(IngredientWithCount.getSeq).toSeq
    ).leftMap(e => NonEmptyList.of(e.toString))
    val value = (energy, item, copyFrom, seq) mapN { (e, result, in1, ins) =>
      val id = if (json.has("id")) new ResourceLocation(JSONUtils.getString(json, "id")) else location
      new EnchantmentCopyRecipe(id, result, e, in1, ins)
    }
    value.leftMap(_.mkString_(", ")).toEither
  }

  def doEnchantedBookMigration(doMigrate: Boolean, outputItem: ItemStack): cats.Endo[CompoundNBT] = {
    if (doMigrate) {
      tag => {
        if (tag.contains("StoredEnchantments") && outputItem.getItem != Items.ENCHANTED_BOOK) {
          val enchantmentTag = tag.getList("StoredEnchantments", NBT.TAG_COMPOUND)
          tag.copy().tap(_.remove("StoredEnchantments")).tap(_.put("Enchantments", enchantmentTag))
        } else {
          tag
        }
      }
    } else {
      identity[CompoundNBT]
    }
  }

  val packetSerialize: WorkbenchRecipes.PacketSerialize = new WorkbenchRecipes.PacketSerialize {
    override def read(recipeId: ResourceLocation, buffer: PacketBuffer): WorkbenchRecipes = {
      val location = buffer.readResourceLocation()
      LOGGER.debug("Serializer loaded {} created from packet.", location)
      val output = buffer.readItemStack()
      val energy = buffer.readLong()
      /*val showInJEI =*/ buffer.readBoolean()

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
      val in1 +: ins = builder.result()
      new EnchantmentCopyRecipe(location, output, energy, in1, ins)
    }

    override def write(buffer: PacketBuffer, recipe: WorkbenchRecipes): Unit = {
      IngredientRecipe.packetSerialize.write(buffer, recipe)
    }
  }
}
