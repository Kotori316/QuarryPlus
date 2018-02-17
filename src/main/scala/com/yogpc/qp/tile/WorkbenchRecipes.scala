package com.yogpc.qp.tile

import java.util.Collections

import com.yogpc.qp.item.ItemTool
import com.yogpc.qp.version.VersionUtil
import com.yogpc.qp.{Config, QuarryPlus}
import net.minecraft.block.Block
import net.minecraft.item.{Item, ItemStack}
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.oredict.OreDictionary

import scala.collection.JavaConverters._
import scala.collection.mutable

abstract sealed class WorkbenchRecipes(val output: ItemDamage, val energy: Double, val showInJEI: Boolean = true) {
    val size: Int

    def inputs: Seq[ItemStack]

    def inputsJ(): java.util.List[ItemStack] = inputs.asJava

    val hasContent: Boolean = true

    override val toString = s"WorkbenchRecipes(output=$output, energy=$energy)"

    override val hashCode: Int = output.hashCode()

    override def equals(obj: scala.Any): Boolean = {
        super.equals(obj) && {
            obj match {
                case r: WorkbenchRecipes => output == r.output && energy == r.energy
                case _ => false
            }
        }
    }
}

private final class R1(o: ItemDamage, e: Double, s: Boolean = true, seq: Seq[(Int => ItemStack)]) extends WorkbenchRecipes(o, e, s) {
    override val size: Int = seq.size

    override def inputs: Seq[ItemStack] = seq.map(_.apply(Config.content.recipe)).filter(VersionUtil.nonEmpty)
}

private final class R2(o: ItemDamage, e: Double, s: Boolean, list: java.util.List[java.util.function.Function[Integer, ItemStack]])
  extends WorkbenchRecipes(o, e, s) {
    private[this] final val seq = list.asScala
    override val size: Int = seq.size

    override def inputs: Seq[ItemStack] = seq.map(_.apply(Config.content.recipe)).filter(VersionUtil.nonEmpty)
}

object WorkbenchRecipes {

    private[this] val recipes = mutable.Map.empty[ItemDamage, WorkbenchRecipes]

    val dummyRecipe: WorkbenchRecipes = new WorkbenchRecipes(ItemDamage.invalid, energy = 0, showInJEI = false) {
        override val inputs: Seq[ItemStack] = Nil
        override val inputsJ: java.util.List[ItemStack] = Collections.emptyList()
        override val size: Int = 0
        override val toString: String = "WorkbenchRecipe NoRecipe"
        override val hasContent: Boolean = false
    }

    def recipeSize: Int = recipes.size

    def removeRecipe(output: ItemDamage): Unit = recipes.remove(output)

    def getRecipe(inputs: java.util.List[ItemStack]): java.util.List[WorkbenchRecipes] = {
        val asScala = inputs.asScala
        recipes.filter { case (_, workRecipe) =>
            workRecipe.inputs.forall(i => {
                asScala.exists(t => OreDictionary.itemMatches(i, t, false) && t.getCount >= i.getCount)
            })
        }.values.toList.asJava
    }

    def addSeqRecipe(output: ItemDamage, energy: Int, inputs: Seq[(Int => ItemStack)])(implicit showInJEI: Boolean = true, unit: EnergyUnit): Unit = {
        val newRecipe = new R1(output, unit.multiple * energy, showInJEI, inputs)
        recipes put(output, newRecipe)
    }

    def addListRecipe(output: ItemDamage, energy: Int, inputs: java.util.List[java.util.function.Function[Integer, ItemStack]])
                     (implicit showInJEI: Boolean, unit: EnergyUnit): Unit = {
        val newRecipe = new R2(output, unit.multiple * energy, showInJEI, inputs)
        recipes put(output, newRecipe)
    }

    def getRecipeMap: Map[ItemDamage, WorkbenchRecipes] = recipes.toMap

    def getRecipeFromResult(stack: ItemStack): java.util.Optional[WorkbenchRecipes] = {
        if (VersionUtil.isEmpty(stack)) return java.util.Optional.empty()
        recipes.get(ItemDamage(stack)).asJava
    }

    protected sealed trait EnergyUnit {
        def multiple: Double
    }

    protected implicit object UnitMJ extends EnergyUnit {
        override val multiple: Double = 1
    }

    object UnitRF extends EnergyUnit {
        override val multiple: Double = 0.1
    }

    protected class F(item: Item, count: Double, damage: Int = 0) extends (Int => ItemStack) {
        override def apply(v1: Int): ItemStack = new ItemStack(item, (count * v1).toInt, damage)
    }

    object F {
        def apply(item: Item, count: Double): (Int => ItemStack) = new F(item, count)

        def apply(item: Item, count: Double, damage: Int): (Int => ItemStack) = new F(item, count, damage)

        def apply(block: Block, count: Double): (Int => ItemStack) = new F(Item.getItemFromBlock(block), count)
    }

    def registerRecipes(): Unit = {
        import com.yogpc.qp.QuarryPlusI._
        import net.minecraft.init.Blocks._
        import net.minecraft.init.Items._
        implicit val showInJEI: Boolean = true
        val bcLoaded = Loader.isModLoaded(QuarryPlus.Optionals.Buildcraft_modID)
        addSeqRecipe(ItemDamage(magicmirror, 1), 32000, Seq(F(ENDER_EYE, 8), F(magicmirror, 1)))
        addSeqRecipe(ItemDamage(magicmirror, 2), 32000, Seq(F(ENDER_EYE, 8), F(magicmirror, 1), F(OBSIDIAN, 4), F(DIRT, 8), F(PLANKS, 8)))
        if (!Config.content.disableController)
            addSeqRecipe(ItemDamage(blockController), 1000000, Seq(F(NETHER_STAR, 1), F(GOLD_INGOT, 40), F(IRON_INGOT, 40), F(ROTTEN_FLESH, 20), F(ARROW, 20), F(BONE, 20), F(GUNPOWDER, 20), F(GHAST_TEAR, 5), F(MAGMA_CREAM, 10), F(BLAZE_ROD, 14), F(CARROT, 2), F(POTATO, 2)))
        addSeqRecipe(ItemDamage(blockMarker), 20000, Seq(F(GOLD_INGOT, 7d / 2d), F(IRON_INGOT, 4), F(REDSTONE, 6), F(DYE, 6, 4), F(GLOWSTONE_DUST, 2), F(ENDER_PEARL, 2d / 5d)))
        addSeqRecipe(ItemDamage(blockQuarry), 320000, Seq(F(DIAMOND, 16), F(GOLD_INGOT, 16), F(IRON_INGOT, 32), F(REDSTONE, 8), F(ENDER_PEARL, 2), F(NETHER_STAR, 3d / 25d)))
        addSeqRecipe(ItemDamage(blockMover), 320000, Seq(F(DIAMOND, 16), F(GOLD_INGOT, 4), F(IRON_INGOT, 4), F(REDSTONE, 24), F(OBSIDIAN, 32), F(ANVIL, 1), F(NETHER_STAR, 1d / 25d), F(ENDER_PEARL, 1)))
        addSeqRecipe(ItemDamage(blockMiningWell), 160000, Seq(F(DIAMOND, 1), F(GOLD_INGOT, 3), F(IRON_INGOT, 16), F(REDSTONE, 8), F(ENDER_PEARL, 1), F(NETHER_STAR, 1d / 25d)))
        addSeqRecipe(ItemDamage(blockPump), 320000, Seq(F(GOLD_INGOT, 8), F(IRON_INGOT, 24), F(REDSTONE, 32), F(GLASS, 256), F(CACTUS, 40), F(NETHER_STAR, 1d / 25d), F(ENDER_PEARL, 2d / 5d)))
        addSeqRecipe(ItemDamage(blockRefinery), 640000, Seq(F(DIAMOND, 18), F(GOLD_INGOT, 12), F(IRON_INGOT, 12), F(GLASS, 64), F(REDSTONE, 16), F(ANVIL, 1), F(OBSIDIAN, 12), F(NETHER_STAR, 1d / 25d), F(ENDER_PEARL, 4d / 5d)))(bcLoaded, UnitMJ)
        addSeqRecipe(ItemDamage(itemTool, 0), 80000, Seq(F(DIAMOND, 2), F(GOLD_INGOT, 8), F(IRON_INGOT, 12), F(REDSTONE, 16), F(DYE, 4, 4), F(OBSIDIAN, 2), F(ENDER_PEARL, 3d / 25d)))
        addSeqRecipe(ItemDamage(ItemTool.getEditorStack), 160000, Seq(F(DIAMOND, 2), F(IRON_INGOT, 8), F(REDSTONE, 2), F(DYE, 8), F(BOOK, 32), F(FEATHER, 1), F(ENDER_PEARL, 1d / 5d)))
        addSeqRecipe(ItemDamage(itemTool, 2), 320000, Seq(F(IRON_INGOT, 32), F(LAVA_BUCKET, 6d / 5d), F(WATER_BUCKET, 6d / 5d), F(ENDER_PEARL, 3d / 25d)))
        addSeqRecipe(ItemDamage(blockBreaker), 320000, Seq(F(DIAMOND, 12), F(GOLD_INGOT, 16), F(IRON_INGOT, 32), F(REDSTONE, 32), F(ENDER_PEARL, 1)))
        addSeqRecipe(ItemDamage(blockPlacer), 320000, Seq(F(DIAMOND, 12), F(GOLD_INGOT, 32), F(IRON_INGOT, 16), F(REDSTONE, 32), F(ENDER_PEARL, 1)))
        addSeqRecipe(ItemDamage(blockLaser), 640000, Seq(F(DIAMOND, 8), F(GOLD_INGOT, 16), F(REDSTONE, 96), F(GLOWSTONE_DUST, 32), F(OBSIDIAN, 16), F(GLASS, 72), F(ENDER_PEARL, 1d / 5d)))(bcLoaded, UnitMJ)
        if (!Config.content.disableChunkDestroyer)
            addSeqRecipe(ItemDamage(blockChunkdestroyer), 3200000, Seq(F(blockQuarry, 3d / 2d), F(blockPump, 1), F(itemTool, 1, 1), F(blockMarker, 3d / 2d), F(DIAMOND_BLOCK, 4), F(EMERALD_BLOCK, 4), F(ENDER_EYE, 32), F(NETHER_STAR, 1), F(net.minecraft.init.Items.SKULL, 24d / 25d, 5)))
        addSeqRecipe(ItemDamage(blockStandalonePump), 3200000, Seq(F(blockPump, 1), F(blockMiningWell, 1), F(blockMarker, 3d / 2d)))
    }
}
