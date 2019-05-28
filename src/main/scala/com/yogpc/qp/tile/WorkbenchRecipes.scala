package com.yogpc.qp.tile

import java.nio.file.{Files, Path}
import java.util.{Collections, Comparator}

import com.google.gson.{Gson, GsonBuilder, JsonArray, JsonObject}
import com.yogpc.qp.block._
import com.yogpc.qp.item.ItemTool
import com.yogpc.qp.utils.{EnableCondition, IngredientWithCount}
import com.yogpc.qp.version.VersionUtil
import com.yogpc.qp.{Config, QuarryPlus}
import net.minecraft.block.Block
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.{JsonUtils, ResourceLocation}
import net.minecraftforge.common.crafting.{CraftingHelper, JsonContext}
import org.apache.commons.io.FilenameUtils

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.Try

abstract sealed class WorkbenchRecipes(val output: ItemDamage, val energy: Double, val showInJEI: Boolean = true)
  extends Ordered[WorkbenchRecipes] {
  val microEnergy = (energy * APowerTile.MicroJtoMJ).toLong
  val size: Int

  def inputs: Seq[Seq[IngredientWithCount]]

  def inputsJ(): java.util.List[java.util.List[IngredientWithCount]] = inputs.map(_.asJava).asJava

  def hasContent: Boolean = true

  val hardCode = true

  def location: ResourceLocation

  def getOutput: ItemStack = output.toStack()

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

  override def compare(that: WorkbenchRecipes) = {
    WorkbenchRecipes.recipeOrdering.compare(this, that)
  }
}

private final class R1(o: ItemDamage, e: Double, s: Boolean = true, seq: Seq[Int => ItemStack], name: Symbol, hasCondition: Boolean) extends WorkbenchRecipes(o, e, s) {
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

private final class R2(override val location: ResourceLocation, o: ItemDamage, e: Double, s: Boolean, list: java.util.List[java.util.function.IntFunction[ItemStack]])
  extends WorkbenchRecipes(o, e, s) {
  private[this] final val seq = list.asScala
  override val size: Int = seq.size

  override def inputs = seq.map(_.apply(Config.content.recipe)).filter(VersionUtil.nonEmpty).map(IngredientWithCount.getSeq)
}

private final class IngredientRecipe(override val location: ResourceLocation, o: ItemStack, e: Double, s: Boolean, seq: Seq[Seq[IngredientWithCount]],
                                     override val hardCode: Boolean = false) extends WorkbenchRecipes(ItemDamage(o), e, s) {
  override val size = seq.size

  override def inputs = seq

  override def getOutput = o.copy()
}

object WorkbenchRecipes {

  private[this] val recipes = mutable.Map.empty[ResourceLocation, WorkbenchRecipes]

  val dummyRecipe: WorkbenchRecipes = new WorkbenchRecipes(ItemDamage.invalid, energy = 0, showInJEI = false) {
    override val inputs = Nil
    override val microEnergy = 0L
    override val inputsJ: java.util.List[java.util.List[IngredientWithCount]] = Collections.emptyList()
    override val size: Int = 0
    override val toString: String = "WorkbenchRecipe NoRecipe"
    override val hasContent: Boolean = false
    override val location: ResourceLocation = new ResourceLocation(QuarryPlus.modID, "builtin_dummy")
  }

  val recipeOrdering: Comparator[WorkbenchRecipes] =
    Ordering.by((a: WorkbenchRecipes) => a.energy) thenComparing Ordering.by((a: WorkbenchRecipes) => Item.getIdFromItem(a.output.item))

  def recipeSize: Int = recipes.size

  def removeRecipe(output: ItemDamage): Unit = recipes.retain { case (_, r) => r.output != output }

  def removeRecipe(location: ResourceLocation): Unit = recipes.remove(location)

  def getRecipe(inputs: java.util.List[ItemStack]): java.util.List[WorkbenchRecipes] = {
    val asScala = inputs.asScala
    recipes.filter {
      case (_, workRecipe) if workRecipe.hasContent =>
        workRecipe.inputs.forall(i => {
          asScala.exists(t => i.exists(_.matches(t)))
        })
      case _ => false
    }.values.toList.sorted.asJava
  }

  def addSeqRecipe(output: ItemDamage, energy: Int, inputs: Seq[Int => ItemStack], name: Symbol = Symbol(""), showInJEI: Boolean = true, unit: EnergyUnit = UnitMJ): Unit = {
    val newRecipe = new R1(output, unit.multiple * energy, showInJEI, inputs, if (name == Symbol("")) Symbol(output.toStack().getUnlocalizedName) else name, name != Symbol(""))
    if (energy > 0)
      recipes put(newRecipe.location, newRecipe)
    else
      QuarryPlus.LOGGER.error(s"Energy of Workbench Recipe is 0. $newRecipe")
  }

  def addListRecipe(location: ResourceLocation, output: ItemDamage, energy: Int, inputs: java.util.List[java.util.function.IntFunction[ItemStack]],
                    showInJEI: Boolean, unit: EnergyUnit): Unit = {
    val newRecipe = new R2(location, output, unit.multiple * energy, showInJEI, inputs)
    if (energy > 0)
      recipes put(location, newRecipe)
    else
      QuarryPlus.LOGGER.error(s"Energy of Workbench Recipe is 0. $newRecipe")
  }

  def addIngredientRecipe(location: ResourceLocation, output: ItemStack, energy: Double, inputs: java.util.List[java.util.List[IngredientWithCount]], hardCode: Boolean): Unit = {
    val scalaInput = inputs.asScala.map(_.asScala.toSeq)
    val newRecipe = new IngredientRecipe(location, output, energy, s = true, scalaInput, hardCode)
    if (energy > 0) {
      recipes put(location, newRecipe)
    } else {
      QuarryPlus.LOGGER.error(s"Energy of Workbench Recipe is 0. $newRecipe")
    }
  }

  def getRecipeMap: Map[ResourceLocation, WorkbenchRecipes] = recipes.toMap

  def getRecipeFromResult(stack: ItemStack): java.util.Optional[WorkbenchRecipes] = {
    if (VersionUtil.isEmpty(stack)) return java.util.Optional.empty()
    val id = ItemDamage(stack)
    recipes.find { case (_, r) => r.output == id }.map(_._2).asJava
  }

  protected sealed trait EnergyUnit {
    def multiple: Double
  }

  protected val UnitMJ: EnergyUnit = new EnergyUnit {
    override val multiple: Double = 1
  }

  val UnitRF: EnergyUnit = new EnergyUnit {
    override val multiple: Double = 0.1
  }

  protected class F(item: Item, count: Double, damage: Int = 0) extends (Int => ItemStack) {
    override def apply(v1: Int): ItemStack = new ItemStack(item, (count * v1).toInt, damage)

    override def toString(): String = item.getUnlocalizedName + "@" + damage + " x" + count
  }

  object F {
    def apply(item: Item, count: Double): Int => ItemStack = new F(item, count)

    def apply(item: Item, count: Double, damage: Int): Int => ItemStack = new F(item, count, damage)

    def apply(block: Block, count: Double): Int => ItemStack = new F(Item.getItemFromBlock(block), count)
  }

  def registerRecipes(): Unit = {
    import com.yogpc.qp.QuarryPlusI._
    import net.minecraft.init.Blocks._
    import net.minecraft.init.Items._
    val map = Map(
      BlockController.SYMBOL -> (ItemDamage(blockController), 1000000, Seq(F(NETHER_STAR, 1), F(GOLD_INGOT, 40), F(IRON_INGOT, 40), F(ROTTEN_FLESH, 20), F(ARROW, 20), F(BONE, 20), F(GUNPOWDER, 20), F(GHAST_TEAR, 5), F(MAGMA_CREAM, 10), F(BLAZE_ROD, 14), F(CARROT, 2), F(POTATO, 2))),
      TileMiningWell.SYMBOL -> (ItemDamage(blockMiningWell), 160000, Seq(F(DIAMOND, 1), F(GOLD_INGOT, 3), F(IRON_INGOT, 16), F(REDSTONE, 8), F(ENDER_PEARL, 1), F(NETHER_STAR, 1d / 25d))),
      BlockBreaker.SYMBOL -> (ItemDamage(blockBreaker), 320000, Seq(F(DIAMOND, 12), F(GOLD_INGOT, 16), F(IRON_INGOT, 32), F(REDSTONE, 32), F(ENDER_PEARL, 1))),
      TileLaser.SYMBOL -> (ItemDamage(blockLaser), 640000, Seq(F(DIAMOND, 8), F(GOLD_INGOT, 16), F(REDSTONE, 96), F(GLOWSTONE_DUST, 32), F(OBSIDIAN, 16), F(GLASS, 72), F(ENDER_PEARL, 1d / 5d))),
      TileAdvQuarry.SYMBOL -> (ItemDamage(blockChunkDestroyer), 3200000, Seq(F(blockQuarry, 3d / 2d), F(blockPump, 1), F(itemTool, 1, 1), F(blockMarker, 3d / 2d), F(DIAMOND_BLOCK, 4), F(EMERALD_BLOCK, 4), F(ENDER_EYE, 32), F(NETHER_STAR, 1), F(net.minecraft.init.Items.SKULL, 24d / 25d, 5))),
      TileAdvPump.SYMBOL -> (ItemDamage(blockStandalonePump), 3200000, Seq(F(blockPump, 1), F(blockMiningWell, 1), F(blockMarker, 3d / 2d))),
      BlockMover.SYMBOL -> (ItemDamage(blockMover), 320000, Seq(F(DIAMOND, 16), F(GOLD_INGOT, 4), F(IRON_INGOT, 4), F(REDSTONE, 24), F(OBSIDIAN, 32), F(ANVIL, 1), F(NETHER_STAR, 1d / 25d), F(ENDER_PEARL, 1))),
      BlockPlacer.SYMBOL -> (ItemDamage(blockPlacer), 320000, Seq(F(DIAMOND, 12), F(GOLD_INGOT, 32), F(IRON_INGOT, 16), F(REDSTONE, 32), F(ENDER_PEARL, 1))),
      Symbol("PumpPlus") -> (ItemDamage(blockPump), 320000, Seq(F(GOLD_INGOT, 8), F(IRON_INGOT, 24), F(REDSTONE, 32), F(GLASS, 256), F(CACTUS, 40), F(NETHER_STAR, 1d / 25d), F(ENDER_PEARL, 2d / 5d))),
      TileMarker.SYMBOL -> (ItemDamage(blockMarker), 20000, Seq(F(GOLD_INGOT, 7d / 2d), F(IRON_INGOT, 4), F(REDSTONE, 6), F(DYE, 6, 4), F(GLOWSTONE_DUST, 2), F(ENDER_PEARL, 2d / 5d))),
      TileRefinery.SYMBOL -> (ItemDamage(blockRefinery), 640000, Seq(F(DIAMOND, 18), F(GOLD_INGOT, 12), F(IRON_INGOT, 12), F(GLASS, 64), F(REDSTONE, 16), F(ANVIL, 1), F(OBSIDIAN, 12), F(NETHER_STAR, 1d / 25d), F(ENDER_PEARL, 4d / 5d))),
      TileQuarry.SYMBOL -> (ItemDamage(blockQuarry), 320000, Seq(F(DIAMOND, 16), F(GOLD_INGOT, 16), F(IRON_INGOT, 32), F(REDSTONE, 8), F(ENDER_PEARL, 2), F(NETHER_STAR, 3d / 25d))),
      BlockBookMover.SYMBOL -> (ItemDamage(blockBookMover), 500000, Seq(F(blockMover, 2), F(BEACON, 1), F(BOOKSHELF, 64), F(DIAMOND, 8))),
      BlockExpPump.SYMBOL -> (ItemDamage(blockExpPump), 320000, Seq(F(GOLD_INGOT, 8), F(IRON_INGOT, 24), F(REDSTONE, 32), F(EXPERIENCE_BOTTLE, 1), F(HAY_BLOCK, 16), F(NETHER_STAR, 1d / 25d), F(ENDER_PEARL, 1))),
      TileReplacer.SYMBOL -> (ItemDamage(blockReplacer), 6400000, Seq(F(WATER_BUCKET, 16), F(LAVA_BUCKET, 16), F(IRON_INGOT, 8), F(GOLD_INGOT, 16), F(REDSTONE, 8), F(ENDER_PEARL, 2), F(ENDER_EYE, 6), F(net.minecraft.init.Items.SKULL, 24d / 25d, 5), F(NETHER_STAR, 4), F(STONE, 512)))
    )
    map.filterKeys(Config.content.enableMap).foreach {
      case (s, (item, energy, recipe)) => addSeqRecipe(item, energy, recipe, name = s)
    }

    val list1 = Seq(
      (ItemDamage(magicMirror, 1), 32000, Seq(F(ENDER_EYE, 8), F(magicMirror, 1))),
      (ItemDamage(magicMirror, 2), 32000, Seq(F(ENDER_EYE, 8), F(magicMirror, 1), F(OBSIDIAN, 4), F(DIRT, 8), F(PLANKS, 8))),
      (ItemDamage(itemTool, 0), 80000, Seq(F(DIAMOND, 2), F(GOLD_INGOT, 8), F(IRON_INGOT, 12), F(REDSTONE, 16), F(DYE, 4, 4), F(OBSIDIAN, 2), F(ENDER_PEARL, 3d / 25d))),
      (ItemDamage(ItemTool.getEditorStack), 160000, Seq(F(DIAMOND, 2), F(IRON_INGOT, 8), F(REDSTONE, 2), F(DYE, 8), F(BOOK, 32), F(FEATHER, 1), F(ENDER_PEARL, 1d / 5d))),
      (ItemDamage(itemTool, 2), 320000, Seq(F(IRON_INGOT, 32), F(LAVA_BUCKET, 6d / 5d), F(WATER_BUCKET, 6d / 5d), F(ENDER_PEARL, 3d / 25d))),
      (ItemDamage(itemTool, 3), 80000, Seq(F(GOLD_INGOT, 16), F(REPEATER, 8), F(COMPARATOR, 4), F(QUARTZ, 32)))
    )
    list1.foreach { case (result, e, recipe) => addSeqRecipe(result, e, recipe) }
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

  def load(objectSeq: Seq[JsonObject], ctx: JsonContext): Seq[WorkbenchRecipes] = {

    objectSeq.filter(json => !json.has("conditions") || CraftingHelper.processConditions(JsonUtils.getJsonArray(json, "conditions"), ctx))
      .filter(json => JsonUtils.getString(json, "type") == QuarryPlus.modID + ":workbench_recipe")
      .flatMap { json =>
        val result = CraftingHelper.getItemStack(JsonUtils.getJsonObject(json, "result"), ctx)
        val id = JsonUtils.getString(json, "id", "")
        val location = if (id == "") QuarryPlus.modID + ":" + JsonUtils.getString(json, "path") else id
        if (VersionUtil.nonEmpty(result)) {
          val recipe = JsonUtils.getJsonArray(json, "ingredients").asScala.map(IngredientWithCount.getSeq(_, ctx)).toSeq
          val energy = Try(JsonUtils.getString(json, "energy", "1000").toDouble).getOrElse(1000d)
          val showInJEI = JsonUtils.getBoolean(json, "showInJEI", true)
          Seq(new IngredientRecipe(new ResourceLocation(location), result, energy, showInJEI, recipe))
        } else {
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
