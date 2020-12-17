package com.yogpc.qp.machines.workbench

import java.nio.charset.StandardCharsets
import java.util.{Collections, Comparator}

import cats.data._
import cats.implicits._
import com.google.gson._
import com.yogpc.qp.utils.{Holder, ItemElement, RecipeGetter}
import com.yogpc.qp.{QuarryPlus, _}
import net.minecraft.item.crafting.{IRecipe, IRecipeSerializer, IRecipeType}
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.network.PacketBuffer
import net.minecraft.resources.{IResource, IResourceManager}
import net.minecraft.util.{JSONUtils, ResourceLocation}
import net.minecraft.world.World
import net.minecraftforge.common.crafting.CraftingHelper
import net.minecraftforge.fml.server.ServerLifecycleHooks
import org.apache.commons.io.IOUtils
import org.apache.logging.log4j.LogManager

import scala.collection.mutable
import scala.jdk.CollectionConverters._

abstract class WorkbenchRecipes(val location: ResourceLocation, val output: ItemElement, val energy: Long, val showInJEI: Boolean = true)
  extends IRecipe[TileWorkbench] with Ordered[WorkbenchRecipes] {
  val microEnergy: Long = energy
  val size: Int

  def inputs: Seq[Seq[IngredientWithCount]]

  def inputsJ(): java.util.List[java.util.List[IngredientWithCount]] = inputs.map(_.asJava).asJava

  def hasContent: Boolean = true

  def getOutput: ItemStack = output.toStack

  def getOutput(inputs: java.util.List[ItemStack]): ItemStack = getOutput

  def subTypeName: String

  override val toString = s"WorkbenchRecipes(output=$output, energy=$energy)"

  override val hashCode: Int = output.hashCode() ^ energy.##

  override def equals(obj: scala.Any): Boolean = {
    super.equals(obj) || {
      obj match {
        case r: WorkbenchRecipes => location == r.location && output == r.output && energy == r.energy
        case _ => false
      }
    }
  }

  override def compare(that: WorkbenchRecipes): Int = WorkbenchRecipes.recipeOrdering.compare(this, that)

  override def matches(inv: TileWorkbench, worldIn: World): Boolean = {
    hasContent && hasAllRequiredItems(inv.inventory.asScala.toSeq)
  }

  def hasAllRequiredItems(inventory: Seq[ItemStack]): Boolean = {
    inputs.forall(in => inventory.exists(invStack => in.exists(_.matches(invStack))))
  }

  override def getCraftingResult(inv: TileWorkbench): ItemStack = getOutput(inv.inventory)

  override def getRecipeOutput: ItemStack = getOutput

  override def canFit(width: Int, height: Int) = true

  override def getSerializer: IRecipeSerializer[_] = WorkbenchRecipes.Serializer

  override def getId: ResourceLocation = location

  override def getType: IRecipeType[WorkbenchRecipes] = WorkbenchRecipes.recipeType

  override def getIcon: ItemStack = new ItemStack(Holder.blockWorkbench)

  override def isDynamic: Boolean = true
}

private object DummyRecipe extends WorkbenchRecipes(
  new ResourceLocation(QuarryPlus.modID, "builtin_dummy"), ItemElement.invalid, energy = 0L, showInJEI = false) {
  override val inputs = Nil
  override val inputsJ: java.util.List[java.util.List[IngredientWithCount]] = Collections.emptyList()
  override val size: Int = 0
  override val toString: String = "WorkbenchRecipe NoRecipe"
  override val hasContent: Boolean = false
  override val subTypeName: String = "dummy"
}

object WorkbenchRecipes {

  val dummyRecipe: WorkbenchRecipes = DummyRecipe

  val recipeOrdering: Comparator[WorkbenchRecipes] =
    Ordering.by((a: WorkbenchRecipes) => a.energy) thenComparing Ordering.by((a: WorkbenchRecipes) => Item.getIdFromItem(a.output.itemDamage.item))

  val recipeLocation = new ResourceLocation(QuarryPlus.modID, "workbench_recipe")
  val recipeType: IRecipeType[WorkbenchRecipes] = IRecipeType.register[WorkbenchRecipes](recipeLocation.toString)
  private[this] final val conditionMessage = "Condition is false"
  final val LOGGER = LogManager.getLogger("QuarryPlus/WorkbenchRecipe")
  private[this] final val recipeParserMapInternal: mutable.Map[String, (JsonObject, ResourceLocation) => Either[String, WorkbenchRecipes]] =
    mutable.Map(IngredientRecipe.subTypeName -> IngredientRecipe.createIngredientRecipe, // Default parser.
      EnchantmentCopyRecipe.subName -> EnchantmentCopyRecipe.createRecipe)
  private[this] final val recipePacketSerializer: mutable.Map[String, PacketSerialize] =
    mutable.Map(IngredientRecipe.subTypeName -> IngredientRecipe.packetSerialize,
      EnchantmentCopyRecipe.subName -> EnchantmentCopyRecipe.packetSerialize)

  /**
   * @return Recipes of workbench, including data pack and vanilla recipe system.
   */
  def recipes: Map[ResourceLocation, WorkbenchRecipes] = {
    Option(ServerLifecycleHooks.getCurrentServer)
      .map(s => RecipeGetter.getRecipes(s.getRecipeManager, recipeType).asScala.toMap).getOrElse(Map.empty)
  }

  def recipeSize: Int = recipes.size

  def getRecipe(inputs: java.util.List[ItemStack]): java.util.List[WorkbenchRecipes] = {
    val asScala = inputs.asScala
    val sorted = recipes.filter {
      case (_, workRecipe) if workRecipe.hasContent =>
        workRecipe.inputs.forall(i => {
          asScala.exists(t => i.exists(_.matches(t)))
        })
      case _ => false
    }.values.toList.sorted
    sorted.asJava
  }

  def getRecipeFromResult(stack: ItemStack): java.util.Optional[WorkbenchRecipes] = {
    if (stack.isEmpty) return java.util.Optional.empty()
    val id = ItemElement(stack)
    recipes.find { case (_, r) => r.output === id }.map(_._2).asJava
  }

  private def resource(resourceManager: IResourceManager, location: ResourceLocation): Either[String, IResource] = {
    Either.fromOption(resourceManager.getAllResources(location).asScala.lastOption, s"Resource: $location isn't found.")
  }

  private def read(r: IResource, gson: Gson): Either[String, JsonObject] = {
    val stream = r.getInputStream
    scala.util.control.Exception.nonFatalCatch[Either[String, JsonObject]]
      .withApply(readEx => Left(readEx.toString))
      .andFinally(stream.close()) {
        val st = IOUtils.toString(stream, StandardCharsets.UTF_8)
        Right(JSONUtils.fromJson(gson, st, classOf[JsonObject]))
      }
  }

  def parse(json: JsonObject, location: ResourceLocation): Either[String, WorkbenchRecipes] = {
    if (CraftingHelper.processConditions(json, "conditions")) {
      val subType: Validated[String, String] = Validated.catchNonFatal(JSONUtils.getString(json, "sub_type", IngredientRecipe.subTypeName))
        .leftMap(_.toString)
      subType.toEither.flatMap(r => getRecipeParser(r).apply(json, location))
    } else {
      Left(conditionMessage)
    }
  }

  private def getRecipeParser(recipeTypeString: String): (JsonObject, ResourceLocation) => Either[String, WorkbenchRecipes] = {
    this.recipeParserMapInternal.getOrElse(recipeTypeString, (_, _) => Left("Not a workbench recipe"))
  }

  object Serializer extends IRecipeSerializer[WorkbenchRecipes] {
    override def read(recipeId: ResourceLocation, json: JsonObject): WorkbenchRecipes = {
      json.addProperty("id", recipeId.toString)
      LOGGER.debug("Serializer loading {} and creating from json.", recipeId)
      parse(json, recipeId) match {
        case Right(value) => value
        case Left(value) if value == conditionMessage => WorkbenchRecipes.dummyRecipe
        case Left(value) => throw new IllegalStateException(s"Recipe loading error. $value, $recipeId")
      }
    }

    override def read(recipeId: ResourceLocation, buffer: PacketBuffer): WorkbenchRecipes = {
      val subName = buffer.readString()
      recipePacketSerializer.get(subName).map(_.read(recipeId, buffer)).getOrElse(DummyRecipe)
    }

    override def write(buffer: PacketBuffer, recipe: WorkbenchRecipes): Unit = {
      val subName = recipe.subTypeName
      buffer.writeString(subName)
      recipePacketSerializer.get(subName).foreach(_.write(buffer, recipe))
    }

    override def setRegistryName(name: ResourceLocation) = throw new UnsupportedOperationException("Changing registry name is not allowed.")

    override def getRegistryName: ResourceLocation = recipeLocation

    override def getRegistryType: Class[IRecipeSerializer[_]] = classOf[IRecipeSerializer[_]]
  }

  trait PacketSerialize {
    def read(recipeId: ResourceLocation, buffer: PacketBuffer): WorkbenchRecipes

    def write(buffer: PacketBuffer, recipe: WorkbenchRecipes): Unit
  }

}
