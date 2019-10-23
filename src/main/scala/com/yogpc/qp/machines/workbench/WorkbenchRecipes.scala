package com.yogpc.qp.machines.workbench

import java.nio.charset.StandardCharsets
import java.util
import java.util.{Collections, Comparator}

import cats._
import cats.data._
import cats.implicits._
import com.google.gson._
import com.yogpc.qp.machines.base.APowerTile
import com.yogpc.qp.utils.{Holder, ItemElement, RecipeGetter}
import com.yogpc.qp.{QuarryPlus, _}
import net.minecraft.client.resources.JsonReloadListener
import net.minecraft.item.crafting.{IRecipe, IRecipeSerializer, IRecipeType}
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.network.PacketBuffer
import net.minecraft.profiler.IProfiler
import net.minecraft.resources.{IResource, IResourceManager}
import net.minecraft.util.{JSONUtils, ResourceLocation}
import net.minecraft.world.World
import net.minecraftforge.common.crafting.CraftingHelper
import net.minecraftforge.fml.server.ServerLifecycleHooks
import org.apache.commons.io.IOUtils

import scala.collection.mutable
import scala.jdk.CollectionConverters._

abstract sealed class WorkbenchRecipes(val location: ResourceLocation, val output: ItemElement, val energy: Long, val showInJEI: Boolean = true)
  extends IRecipe[TileWorkbench] with Ordered[WorkbenchRecipes] {
  val microEnergy = energy
  val size: Int

  def inputs: Seq[Seq[IngredientWithCount]]

  def inputsJ(): java.util.List[java.util.List[IngredientWithCount]] = inputs.map(_.asJava).asJava

  def hasContent: Boolean = true

  def getOutput: ItemStack = output.toStack

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

  override def compare(that: WorkbenchRecipes) = WorkbenchRecipes.recipeOrdering.compare(this, that)

  override def matches(inv: TileWorkbench, worldIn: World) = {
    val inputInv = Range(0, inv.getSizeInventory).map(inv.getStackInSlot)
    hasContent && inputs.forall(in => inputInv.exists(invStack => in.exists(_.matches(invStack))))
  }

  override def getCraftingResult(inv: TileWorkbench) = getOutput

  override def getRecipeOutput = getOutput

  override def canFit(width: Int, height: Int) = true

  override def getSerializer = WorkbenchRecipes.Serializer

  override def getId = location

  override def getType = WorkbenchRecipes.recipeType

  override def getIcon = new ItemStack(Holder.blockWorkbench)
}

private final class IngredientRecipe(location: ResourceLocation, o: ItemStack, e: Long, s: Boolean, seq: Seq[Seq[IngredientWithCount]])
  extends WorkbenchRecipes(location, ItemElement(o), e, s) {
  override val size = seq.size

  override def inputs = seq

  override def getOutput = o.copy()
}

private object DummyRecipe extends WorkbenchRecipes(
  new ResourceLocation(QuarryPlus.modID, "builtin_dummy"), ItemElement.invalid, energy = 0, showInJEI = false) {
  override val inputs = Nil
  override val microEnergy = 0L
  override val inputsJ: java.util.List[java.util.List[IngredientWithCount]] = Collections.emptyList()
  override val size: Int = 0
  override val toString: String = "WorkbenchRecipe NoRecipe"
  override val hasContent: Boolean = false
}

object WorkbenchRecipes {

  /**
   * JVM static instance. Need to be reset when world is changed or entered into multi player world.
   */
  private[this] val recipes_internal = mutable.Map.empty[ResourceLocation, WorkbenchRecipes]

  val dummyRecipe: WorkbenchRecipes = DummyRecipe

  val recipeOrdering: Comparator[WorkbenchRecipes] =
    Ordering.by((a: WorkbenchRecipes) => a.energy) thenComparing Ordering.by((a: WorkbenchRecipes) => Item.getIdFromItem(a.output.itemDamage.item))

  val recipeLocation = new ResourceLocation(QuarryPlus.modID, "workbench_recipe")
  val recipeType = IRecipeType.register[WorkbenchRecipes](recipeLocation.toString)
  private[this] final val conditionMessage = "Condition is false"

  def recipes: Map[ResourceLocation, WorkbenchRecipes] = {
    Option(ServerLifecycleHooks.getCurrentServer)
      .map(s => RecipeGetter.getRecipes(s.getRecipeManager, recipeType).asScala.toMap).getOrElse(Map.empty) ++ recipes_internal
  }

  def recipeSize: Int = recipes.size

  def removeRecipe(output: ItemElement): Unit = recipes_internal.filterInPlace { case (_, r) => r.output =!= output }

  def removeRecipe(location: ResourceLocation): Unit = recipes_internal.remove(location)

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

  def addIngredientRecipe(location: ResourceLocation, output: ItemStack, energy: Double, inputs: java.util.List[java.util.List[IngredientWithCount]]): Unit = {
    val scalaInput = inputs.asScala.map(_.asScala.toSeq).toSeq
    val newRecipe = new IngredientRecipe(location, output, (energy * APowerTile.MJToMicroMJ).toLong, s = true, scalaInput)
    if (energy > 0) {
      recipes_internal put(location, newRecipe)
    } else {
      QuarryPlus.LOGGER.error(s"Energy of Workbench Recipe is 0. $newRecipe")
    }
  }

  def getRecipeMap: Map[ResourceLocation, WorkbenchRecipes] = recipes

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

  private def parse(json: JsonObject, location: ResourceLocation): Either[String, WorkbenchRecipes] = {
    type EOR[A] = ValidatedNel[String, A]
    implicit val ff: Functor[EOR] with Semigroupal[EOR] = new Functor[EOR] with Semigroupal[EOR] {
      override def map[A, B](fa: EOR[A])(f: A => B): EOR[B] = fa map f

      override def product[A, B](fa: EOR[A], fb: EOR[B]): EOR[(A, B)] = fa product fb
    }
    val cond: EOR[Unit] = Validated.condNel(CraftingHelper.processConditions(json, "conditions"), (), conditionMessage)
    val recipeType: EOR[Unit] = Validated.condNel(JSONUtils.getString(json, "type") == recipeLocation.toString, (), "Not a workbench recipe")
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
    val value = (cond, recipeType, energy, item, seq).mapN { case (_, _, e, stack, recipe) =>
      val showInJei = JSONUtils.getBoolean(json, "showInJEI", true)
      val id = JSONUtils.getString(json, "id", "").some.filter(_.nonEmpty).map(new ResourceLocation(_)).getOrElse(location)
      new IngredientRecipe(id, stack, e.toLong, showInJei, recipe)
    }
    value.leftMap(_.mkString_(", ")).toEither
  }

  def registerJsonRecipe(resourceManager: IResourceManager): Unit = {
    val gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping.create
    for (location <- resourceManager.getAllResourceLocations("quarryplus/workbench", s => s.endsWith(".json") && !s.startsWith("_")).asScala) {
      resource(resourceManager, location) flatMap (read(_, gson)) flatMap (parse(_, location)) match {
        case Left(value) if value == conditionMessage =>
        case Left(value) => QuarryPlus.LOGGER.error(s"Error in loading $location, $value")
        case Right(r) => recipes_internal.put(r.location, r)
      }
    }
  }

  object Serializer extends IRecipeSerializer[WorkbenchRecipes] {
    override def read(recipeId: ResourceLocation, json: JsonObject): WorkbenchRecipes = {
      json.addProperty("id", recipeId.toString)
      parse(json, recipeId) match {
        case Right(value) => value
        case Left(value) if value == conditionMessage => WorkbenchRecipes.dummyRecipe
        case Left(value) => throw new IllegalStateException(s"Recipe loading error. $value, $recipeId")
      }
    }

    override def read(recipeId: ResourceLocation, buffer: PacketBuffer): WorkbenchRecipes = {
      val location = buffer.readResourceLocation()
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

    override def setRegistryName(name: ResourceLocation) = throw new UnsupportedOperationException("Changing registry name is not allowed.")

    override def getRegistryName = recipeLocation

    override def getRegistryType = classOf[IRecipeSerializer[_]]
  }

  object Reload extends JsonReloadListener(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping.create, QuarryPlus.modID + "/workbench") {
    override def apply(splashList: util.Map[ResourceLocation, JsonObject], resourceManagerIn: IResourceManager, profilerIn: IProfiler): Unit = {
      recipes_internal.clear()

      for ((location, jsonObject) <- splashList.asScala) {
        parse(jsonObject, location) match {
          case Left(value) if value == conditionMessage =>
          case Left(value) => QuarryPlus.LOGGER.error(s"Error in loading $location, $value")
          case Right(r) => recipes_internal.put(r.location, r)
        }
      }

      QuarryPlus.LOGGER.debug("Recipe loaded.")
    }
  }

}
