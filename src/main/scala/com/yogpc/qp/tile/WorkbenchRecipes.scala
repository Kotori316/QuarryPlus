package com.yogpc.qp.tile

import com.yogpc.qp.item.ItemTool
import com.yogpc.qp.version.VersionUtil
import com.yogpc.qp.{Config, QuarryPlus}
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.oredict.OreDictionary

import scala.collection.JavaConverters._
import scala.collection.mutable

abstract sealed class WorkbenchRecipes(val output: ItemDamage, val energy: Double, val showInJEI: Boolean = true) {
    val size: Int

    def inputs: Seq[ItemStack]

    def inputsJ() = inputs.asJava

    override def toString = s"WorkbenchRecipes(output=$output, energy=$energy)"

    override def hashCode(): Int = output.hashCode()

    override def equals(obj: scala.Any): Boolean = {
        super.equals(obj) && {
            obj match {
                case r: WorkbenchRecipes => output == r.output && energy == r.energy
                case _ => false
            }
        }
    }
}

private final class R1(o: ItemDamage, e: Double, s: Boolean = true, val seq: Seq[(Int => ItemStack)]) extends WorkbenchRecipes(o, e, s) {
    override val size: Int = seq.size

    override def inputs: Seq[ItemStack] = seq.map(_.apply(Config.content.recipe)).filter(VersionUtil.nonEmpty)
}

private final class R2(o: ItemDamage, e: Double, s: Boolean = true,
                       list: java.util.List[java.util.function.Function[Integer, ItemStack]]) extends WorkbenchRecipes(o, e, s) {
    val seq = list.asScala
    override val size: Int = seq.size

    override def inputs: Seq[ItemStack] = seq.map(_.apply(Config.content.recipe)).filter(VersionUtil.nonEmpty)
}

object WorkbenchRecipes {

    private[this] val recipes = mutable.Map.empty[ItemDamage, WorkbenchRecipes]

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

    def addRecipe(output: ItemDamage, energy: Int, inputs: Seq[(Int => ItemStack)])(implicit showInJEI: Boolean = true, unit: EnergyUnit): Unit = {
        val newRecipe = new R1(output, unit.multiple * energy, showInJEI, inputs)
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

    protected object UnitRF extends EnergyUnit {
        override val multiple: Double = 0.1
    }

    def registerRecipes(): Unit = {
        import com.yogpc.qp.QuarryPlusI._
        import net.minecraft.init.Blocks._
        import net.minecraft.init.Items._
        implicit val showInJEI: Boolean = true
        val bcLoaded = Loader.isModLoaded(QuarryPlus.Optionals.Buildcraft_modID)
        addRecipe(ItemDamage(magicmirror, 1), 32000, Seq(i => new ItemStack(ENDER_EYE, i * 8), i => new ItemStack(magicmirror, i * 1)))
        addRecipe(ItemDamage(magicmirror, 2), 32000, Seq(i => new ItemStack(ENDER_EYE, i * 8), i => new ItemStack(magicmirror, i * 1), i => new ItemStack(OBSIDIAN, i * 4), i => new ItemStack(DIRT, i * 8), i => new ItemStack(PLANKS, i * 8)))
        if (!Config.content.disableController)
            addRecipe(ItemDamage(blockController), 1000000, Seq(i => new ItemStack(NETHER_STAR, i * 2), i => new ItemStack(ROTTEN_FLESH, i * 40), i => new ItemStack(ARROW, i * 40), i => new ItemStack(BONE, i * 40), i => new ItemStack(GUNPOWDER, i * 40), i => new ItemStack(IRON_INGOT, i * 80), i => new ItemStack(GOLD_INGOT, i * 40), i => new ItemStack(GHAST_TEAR, i * 10), i => new ItemStack(MAGMA_CREAM, i * 20), i => new ItemStack(BLAZE_ROD, i * 28), i => new ItemStack(CARROT, i * 2), i => new ItemStack(POTATO, i * 2)))
        addRecipe(ItemDamage(blockMarker), 20000, Seq(i => new ItemStack(REDSTONE, i * 12), i => new ItemStack(DYE, i * 12, 4), i => new ItemStack(GOLD_INGOT, i * 7), i => new ItemStack(IRON_INGOT, i * 8), i => new ItemStack(GLOWSTONE_DUST, i * 2), i => new ItemStack(ENDER_PEARL, i * 2 / 5)))
        addRecipe(ItemDamage(blockQuarry), 320000, Seq(i => new ItemStack(DIAMOND, i * 32), i => new ItemStack(GOLD_INGOT, i * 32), i => new ItemStack(IRON_INGOT, i * 64), i => new ItemStack(REDSTONE, i * 18), i => new ItemStack(ENDER_PEARL, i * 2), i => new ItemStack(NETHER_STAR, i * 3 / 25)))
        addRecipe(ItemDamage(blockMover), 320000, Seq(i => new ItemStack(OBSIDIAN, i * 64), i => new ItemStack(DIAMOND, i * 32), i => new ItemStack(ANVIL, i * 2), i => new ItemStack(REDSTONE, i * 48), i => new ItemStack(GOLD_INGOT, i * 8), i => new ItemStack(IRON_INGOT, i * 8), i => new ItemStack(NETHER_STAR, i * 1 / 25), i => new ItemStack(ENDER_PEARL, i * 1)))
        addRecipe(ItemDamage(blockMiningWell), 160000, Seq(i => new ItemStack(IRON_INGOT, i * 32), i => new ItemStack(REDSTONE, i * 16), i => new ItemStack(DIAMOND, i * 4), i => new ItemStack(ENDER_PEARL, i * 2), i => new ItemStack(NETHER_STAR, i * 1 / 25), i => new ItemStack(GOLD_INGOT, i * 1)))
        addRecipe(ItemDamage(blockPump), 320000, Seq(i => new ItemStack(IRON_INGOT, i * 48), i => new ItemStack(REDSTONE, i * 64), i => new ItemStack(GLASS, i * 256), i => new ItemStack(CACTUS, i * 40), i => new ItemStack(GOLD_INGOT, i * 16), i => new ItemStack(NETHER_STAR, i * 1 / 25), i => new ItemStack(ENDER_PEARL, i * 2 / 5)))
        addRecipe(ItemDamage(blockRefinery), 640000, Seq(i => new ItemStack(DIAMOND, i * 36), i => new ItemStack(GOLD_INGOT, i * 24), i => new ItemStack(IRON_INGOT, i * 24), i => new ItemStack(GLASS, i * 128), i => new ItemStack(REDSTONE, i * 32), i => new ItemStack(ANVIL, i * 2), i => new ItemStack(OBSIDIAN, i * 24), i => new ItemStack(NETHER_STAR, i * 1 / 25), i => new ItemStack(ENDER_PEARL, i * 4 / 5)))(bcLoaded, UnitMJ)
        addRecipe(ItemDamage(itemTool, 0), 80000, Seq(i => new ItemStack(GOLD_INGOT, i * 16), i => new ItemStack(IRON_INGOT, i * 24), i => new ItemStack(OBSIDIAN, i * 4), i => new ItemStack(DIAMOND, i * 4), i => new ItemStack(REDSTONE, i * 16), i => new ItemStack(DYE, i * 4, 4), i => new ItemStack(ENDER_PEARL, i * 3 / 25)))
        addRecipe(ItemDamage(ItemTool.getEditorStack), 160000, Seq(i => new ItemStack(IRON_INGOT, i * 16), i => new ItemStack(BOOK, i * 64), i => new ItemStack(FEATHER, i * 2), i => new ItemStack(DYE, i * 16), i => new ItemStack(DIAMOND, i * 4), i => new ItemStack(REDSTONE, i * 4), i => new ItemStack(ENDER_PEARL, i * 1 / 5)))
        addRecipe(ItemDamage(itemTool, 2), 320000, Seq(i => new ItemStack(IRON_INGOT, i * 64), i => new ItemStack(LAVA_BUCKET, i * 12 / 5), i => new ItemStack(WATER_BUCKET, i * 12 / 5), i => new ItemStack(ENDER_PEARL, i * 3 / 25)))
        addRecipe(ItemDamage(blockBreaker), 320000, Seq(i => new ItemStack(REDSTONE, i * 64), i => new ItemStack(DIAMOND, i * 24), i => new ItemStack(GOLD_INGOT, i * 32), i => new ItemStack(IRON_INGOT, i * 64), i => new ItemStack(ENDER_PEARL, i * 2)))
        addRecipe(ItemDamage(blockPlacer), 320000, Seq(i => new ItemStack(REDSTONE, i * 64), i => new ItemStack(DIAMOND, i * 24), i => new ItemStack(GOLD_INGOT, i * 64), i => new ItemStack(IRON_INGOT, i * 32), i => new ItemStack(ENDER_PEARL, i * 2)))
        addRecipe(ItemDamage(blockLaser), 640000, Seq(i => new ItemStack(DIAMOND, i * 16), i => new ItemStack(REDSTONE, i * 192), i => new ItemStack(OBSIDIAN, i * 32), i => new ItemStack(GLASS, i * 128), i => new ItemStack(GLOWSTONE_DUST, i * 64), i => new ItemStack(GOLD_INGOT, i * 32), i => new ItemStack(ENDER_PEARL, i * 1 / 5)))(bcLoaded, UnitMJ)
        if (!Config.content.disableChunkDestroyer)
            addRecipe(ItemDamage(blockChunkdestroyer), 3200000, Seq(i => new ItemStack(blockQuarry, i * 3), i => new ItemStack(blockPump, i * 2), i => new ItemStack(itemTool, i * 1, 1), i => new ItemStack(blockMarker, i * 3), i => new ItemStack(DIAMOND_BLOCK, i * 8), i => new ItemStack(EMERALD_BLOCK, i * 8), i => new ItemStack(ENDER_EYE, i * 64), i => new ItemStack(NETHER_STAR, i * 1), i => new ItemStack(net.minecraft.init.Items.SKULL, i * 24 / 25, 5)))
        addRecipe(ItemDamage(blockStandalonePump), 3200000, Seq(i => new ItemStack(blockPump, i * 2), i => new ItemStack(blockMiningWell, i * 2), i => new ItemStack(blockMarker, i * 3)))
    }
}
