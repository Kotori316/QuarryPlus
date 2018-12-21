package com.yogpc.qp.integration.jei

import java.util.Collections

import com.yogpc.qp._
import com.yogpc.qp.integration.jei.MoverRecipeWrapper.MoverRecipe
import com.yogpc.qp.item.IEnchantableItem
import mezz.jei.api.ingredients.{IIngredients, VanillaTypes}
import mezz.jei.api.recipe.IRecipeWrapper
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.I18n
import net.minecraft.enchantment.Enchantment
import net.minecraft.init.Items
import net.minecraft.item.{Item, ItemStack}
import net.minecraftforge.fml.common.registry.ForgeRegistries

import scala.collection.JavaConverters._

class MoverRecipeWrapper(recipe: MoverRecipe) extends IRecipeWrapper {

  val enchantments = MoverRecipeWrapper.enchantments.filter(recipe)

  override def getIngredients(ingredients: IIngredients): Unit = {
    val input = enchantments.map(e => new ItemStack(Items.DIAMOND_PICKAXE).tap(_.addEnchantment(e, e.getMaxLevel)))
    val output = enchantments.map(e => recipe.toStack.tap(_.addEnchantment(e, e.getMaxLevel)))

    ingredients.setInputLists(VanillaTypes.ITEM, Collections.singletonList(input.asJava))
    ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(output.asJava))
  }

  override def drawInfo(minecraft: Minecraft, recipeWidth: Int, recipeHeight: Int, mouseX: Int, mouseY: Int): Unit = {
    import MoverRecipeCategory._
    enchantments.zipWithIndex.foreach { case (e, i) =>
      minecraft.fontRenderer.drawString(I18n.format(e.getName), 36 - xOff, 6 - yOff + 10 * i, 0x404040)
    }
  }
}

object MoverRecipeWrapper {

  def recipes: java.util.Collection[MoverRecipe] =
    ForgeRegistries.ITEMS.asScala
      .collect { case i: Item with IEnchantableItem => i }
      .flatMap(wrap)
      .toSeq.asJava

  import net.minecraft.init.Enchantments._

  val enchantments = Seq(EFFICIENCY, UNBREAKING, FORTUNE, SILK_TOUCH)

  def wrap(item: Item with IEnchantableItem): Seq[MoverRecipe] = item.stacks().map(s => MoverRecipe(item, s))

  case class MoverRecipe(item: Item with IEnchantableItem, stack: ItemStack) extends (Enchantment => Boolean) {
    def toStack: ItemStack = stack.copy()

    def canMove(enchantment: Enchantment): Boolean = item.canMove(toStack, enchantment)

    override def apply(v1: Enchantment): Boolean = canMove(v1)
  }

}
