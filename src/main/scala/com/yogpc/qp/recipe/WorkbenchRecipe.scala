package com.yogpc.qp.recipe

import com.google.gson.{Gson, GsonBuilder, JsonArray, JsonObject}
import com.yogpc.qp.tile._
import com.yogpc.qp.utils.{EnableCondition, IngredientWithCount}
import com.yogpc.qp.version.VersionUtil
import com.yogpc.qp.{Config, QuarryPlus}
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.{JsonUtils, ResourceLocation}
import net.minecraftforge.common.crafting.{CraftingHelper, JsonContext}
import org.apache.commons.io.FilenameUtils

import java.nio.file.{Files, Path}
import java.util.Collections
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.{Failure, Success, Try}

abstract class WorkbenchRecipe(val output: ItemDamage, val energy: Double, val showInJEI: Boolean = true) {
  val microEnergy = (energy * APowerTile.MJToMicroMJ).toLong
  val size: Int

  def inputs: Seq[Seq[IngredientWithCount]]

  def inputsJ(): java.util.List[java.util.List[IngredientWithCount]] = inputs.map(_.asJava).asJava

  def hasContent: Boolean = true

  val hardCode = true

  def location: ResourceLocation

  def getOutput: ItemStack = output.toStack()

  def getOutput(input: java.util.List[ItemStack]): ItemStack = getOutput

  override val toString = s"WorkbenchRecipes(output=$output, energy=$energy)"

  override val hashCode: Int = output.hashCode() ^ energy.##

  override def equals(obj: scala.Any): Boolean = {
    super.equals(obj) || {
      obj match {
        case r: WorkbenchRecipe => location == r.location && output == r.output && energy == r.energy
        case _ => false
      }
    }
  }

}

private final class R1(o: ItemDamage, e: Double, s: Boolean = true, seq: Seq[Int => ItemStack], name: Symbol, hasCondition: Boolean) extends WorkbenchRecipe(o, e, s) {
  override val size: Int = seq.size

  override def inputs = seq.map(_.apply(Config.content.recipe)).filter(VersionUtil.nonEmpty).map(IngredientWithCount.getSeq)

  override def hasContent = Config.content.useHardCodedRecipe

  override val location: ResourceLocation = new ResourceLocation(QuarryPlus.modID, "builtin_" + name.name)

  def toJson = {
    def stackToJson(stack: ItemStack) = {
      val e = new JsonObject
      e.addProperty("item", stack.getItem.getRegistryName.toString)
      e.addProperty("data", stack.getMetadata)
      e.addProperty("count", VersionUtil.getCount(stack))
      if (stack.hasTagCompound) {
        e.add("nbt", new GsonBuilder().disableHtmlEscaping().create().fromJson(stack.getTagCompound.toString, classOf[JsonObject]))
      }
      e
    }

    val json = new JsonObject
    json.addProperty("id", location.toString)
    json.addProperty("type", QuarryPlus.modID + ":workbench_recipe")
    val ingredients = new JsonArray
    seq.map(_.apply(Config.content.recipe)).filter(VersionUtil.nonEmpty).map(stackToJson).foreach(ingredients.add)
    json.add("ingredients", ingredients)
    json.addProperty("energy", energy)
    json.addProperty("showInJEI", showInJEI)
    json.add("result", stackToJson(output.toStack()))
    if (hasCondition) {
      val conditions = new JsonArray
      val c1 = new JsonObject
      c1.addProperty("type", EnableCondition.NAME)
      c1.addProperty("value", name.name)
      conditions.add(c1)
      json.add("conditions", conditions)
    }
    json
  }
}

object WorkbenchRecipe extends RecipeSearcher {

  private[this] val recipes = mutable.Map.empty[ResourceLocation, WorkbenchRecipe]

  val dummyRecipe: WorkbenchRecipe = new WorkbenchRecipe(ItemDamage.invalid, energy = 0, showInJEI = false) {
    override val inputs = Nil
    override val microEnergy = 0L
    override val inputsJ: java.util.List[java.util.List[IngredientWithCount]] = Collections.emptyList()
    override val size: Int = 0
    override val toString: String = "WorkbenchRecipe NoRecipe"
    override val hasContent: Boolean = false
    override val location: ResourceLocation = new ResourceLocation(QuarryPlus.modID, "builtin_dummy")
  }

  implicit val recipeOrdering: Ordering[WorkbenchRecipe] = Ordering.comparatorToOrdering(
    Ordering.by((a: WorkbenchRecipe) => a.energy) thenComparing Ordering.by((a: WorkbenchRecipe) => Item.getIdFromItem(a.output.item))
  )

  def recipeSize: Int = recipes.size

  def removeRecipe(output: ItemDamage): Unit = recipes.retain { case (_, r) => r.output != output }

  def removeRecipe(location: ResourceLocation): Unit = recipes.remove(location)

  override def getRecipe(inputs: java.util.List[ItemStack]): java.util.List[WorkbenchRecipe] = {
    val asScala = inputs.asScala
    recipes.filter {
      case (_, workRecipe) if workRecipe.hasContent =>
        workRecipe.inputs.forall(i => {
          asScala.exists(t => i.exists(_.matches(t)))
        })
      case _ => false
    }.values.toList.sorted.asJava
  }

  private def addRecipe(recipe: WorkbenchRecipe): Unit = {
    if (recipe.energy > 0)
      recipes put(recipe.location, recipe)
    else
      QuarryPlus.LOGGER.error(s"Energy of Workbench Recipe is less than 0. $recipe")
  }

  def addSeqRecipe(output: ItemDamage, energy: Int, inputs: Seq[Int => ItemStack], name: Symbol = Symbol(""), showInJEI: Boolean = true, unit: EnergyUnit = EnergyUnit.MJ): Unit = {
    val newRecipe = new R1(output, unit.multiple * energy, showInJEI, inputs, if (name == Symbol("")) Symbol(output.toStack().getUnlocalizedName) else name, name != Symbol(""))
    if (energy > 0)
      recipes put(newRecipe.location, newRecipe)
    else
      QuarryPlus.LOGGER.error(s"Energy of Workbench Recipe is 0. $newRecipe")
  }

  def addListRecipe(location: ResourceLocation, output: ItemDamage, energy: Int, inputs: java.util.List[java.util.function.IntFunction[ItemStack]],
                    showInJEI: Boolean, unit: EnergyUnit): Unit = {
    val recipeInput = inputs.asScala.map(_.apply(Config.content.recipe)).filter(VersionUtil.nonEmpty).map(IngredientWithCount.getSeq)
    addIngredientRecipe(location, output.toStack(), unit.multiple * energy, recipeInput, hardCode = false, showInJEI = showInJEI)
  }

  def addIngredientRecipe(location: ResourceLocation, output: ItemStack, energy: Double, inputs: java.util.List[java.util.List[IngredientWithCount]], hardCode: Boolean): Unit = {
    addIngredientRecipe(location, output, energy, inputs.asScala.map(_.asScala.toSeq), hardCode, showInJEI = true)
  }

  def addIngredientRecipe(location: ResourceLocation, output: ItemStack, energy: Double, inputs: Seq[Seq[IngredientWithCount]], hardCode: Boolean, showInJEI: Boolean): Unit = {
    val newRecipe = new IngredientRecipe(location, output, energy, showInJEI, inputs, hardCode)
    if (energy > 0) {
      recipes put(location, newRecipe)
    } else {
      QuarryPlus.LOGGER.error(s"Energy of Workbench Recipe is 0. $newRecipe")
    }
  }

  def addEnchantmentCopyRecipe(location: ResourceLocation, output: ItemStack, energy: Double, input: IngredientWithCount): Unit = {
    val newRecipe = new EnchantmentCopyRecipe(location, output, energy, input)
    addRecipe(newRecipe)
  }

  override def getRecipeMap: Map[ResourceLocation, WorkbenchRecipe] = recipes.toMap

  def getRecipeFromResult(stack: ItemStack): java.util.Optional[WorkbenchRecipe] = {
    if (VersionUtil.isEmpty(stack)) return java.util.Optional.empty()
    val id = ItemDamage(stack)
    recipes.find { case (_, r) => r.output == id }.map(_._2).asJava
  }

  def registerJsonRecipe(path: java.util.List[Path]): Unit = {
    recipes.retain { case (_, r) => r.hardCode }
    //    val pathFilter: Path => Boolean = path => !startWith("_")(path) && endWith(".json")(path)
    val ctx = new JsonContext(QuarryPlus.modID)
    val gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping.create
    load(path.asScala
      .filterNot(startWith("_"))
      .map(p => pathToJson(p, gson)), ctx)
      .foreach(r => recipes put(r.location, r))
  }

  private def pathToJson(p: Path, gson: Gson): JsonObject = {
    val reader = Files.newBufferedReader(p)
    val json = JsonUtils.fromJson(gson, Files.newBufferedReader(p), classOf[JsonObject])
    reader.close()
    json.addProperty("path", FilenameUtils.getBaseName(p.toString))
    json
  }

  def load(objectSeq: Seq[JsonObject], ctx: JsonContext): Seq[WorkbenchRecipe] = {

    objectSeq.filter(json => !json.has("conditions") || CraftingHelper.processConditions(JsonUtils.getJsonArray(json, "conditions"), ctx))
      .filter(json => JsonUtils.getString(json, "type") == QuarryPlus.modID + ":workbench_recipe")
      .flatMap { json =>
        val id = JsonUtils.getString(json, "id", "")
        val location = if (id == "") QuarryPlus.modID + ":" + JsonUtils.getString(json, "path") else id
        val resultTry = Try(CraftingHelper.getItemStack(JsonUtils.getJsonObject(json, "result"), ctx))
          .flatMap(i => if (VersionUtil.nonEmpty(i)) Success(i) else Failure(new IllegalArgumentException(s"result item is empty, id=$location")))
        resultTry match {
          case Success(result) =>
            val recipe = JsonUtils.getJsonArray(json, "ingredients").asScala.map(IngredientWithCount.getSeq(_, ctx)).toSeq
            val energy = Try(JsonUtils.getString(json, "energy", "1000").toDouble).getOrElse(1000d)
            val showInJEI = JsonUtils.getBoolean(json, "showInJEI", true)
            Seq(new IngredientRecipe(new ResourceLocation(location), result, energy, showInJEI, recipe))
          case Failure(exception) =>
            QuarryPlus.LOGGER.error(s"Caught in loading recipe $location", exception)
            Seq.empty
        }
      }
      .filter(_.energy > 0)
  }

  def outputDefaultRecipe(directory: Path): Unit = {
    val gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping.create
    getRecipeMap.collect { case (_, r: R1) => (r.output.toString + ".json", r.toJson) }
      .map { case (s, j) => (directory.resolve(s), gson.toJson(j).split(System.lineSeparator()).toSeq) }
      .foreach { case (outPath, s) =>
        import scala.collection.JavaConverters._
        Files.write(outPath, s.asJava)
      }
  }

  private val startWith: String => Path => Boolean = name => path => path.getFileName.toString.startsWith(name)
}
