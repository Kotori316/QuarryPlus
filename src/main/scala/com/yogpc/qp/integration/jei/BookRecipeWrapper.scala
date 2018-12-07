package com.yogpc.qp.integration.jei

import java.util.Collections

import com.yogpc.qp._
import com.yogpc.qp.integration.jei.BookRecipeWrapper.BookRecipe
import com.yogpc.qp.item.IEnchantableItem
import mezz.jei.api.ingredients.IIngredients
import mezz.jei.api.recipe.IRecipeWrapper
import net.minecraft.client.Minecraft
import net.minecraft.enchantment.{EnchantmentData, EnumEnchantmentType}
import net.minecraft.init.Items
import net.minecraft.item.{Item, ItemEnchantedBook, ItemStack}
import net.minecraftforge.fml.common.registry.ForgeRegistries

import scala.collection.JavaConverters._

class BookRecipeWrapper(recipe: BookRecipe) extends IRecipeWrapper {

  import BookRecipeWrapper._

  override def getIngredients(ingredients: IIngredients): Unit = {
    val input = Seq(items.asJava, Collections.singletonList(ItemEnchantedBook.getEnchantedItemStack(recipe.ench))).asJava
    val output = items.map(_.copy()).map(_.tap(_.addEnchantment(recipe.ench.enchantment, recipe.ench.enchantmentLevel))).asJava

    ingredients.setInputLists(classOf[ItemStack], input)
    ingredients.setOutput(classOf[ItemStack], output)
  }

  override def drawInfo(minecraft: Minecraft, recipeWidth: Int, recipeHeight: Int, mouseX: Int, mouseY: Int): Unit = {
    import BookRecipeCategory._
    super.drawInfo(minecraft, recipeWidth, recipeHeight, mouseX, mouseY)
    minecraft.fontRenderer.drawString(50000 + "MJ", 36 - xOff, 66 - yOff, 0x404040)
  }
}

object BookRecipeWrapper {
  val enchTypes = EnumEnchantmentType.values().filter(_.canEnchantItem(Items.DIAMOND_PICKAXE)).toSet
  val enchantments = ForgeRegistries.ENCHANTMENTS.getValuesCollection.asScala.filter(e => enchTypes(e.`type`)).map(e => new EnchantmentData(e, e.getMaxLevel))
  val items = ForgeRegistries.ITEMS.asScala.collect { case i: Item with IEnchantableItem if i.isValidInBookMover => i }.flatMap(_.stacks).toSeq

  def recipes = enchantments.map(BookRecipe.apply).toSeq.asJava

  case class BookRecipe(ench: EnchantmentData)

}
