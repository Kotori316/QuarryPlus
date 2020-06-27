package com.yogpc.qp.integration.jei
/*
import java.util.Collections

import com.yogpc.qp.machines.base.IEnchantableItem
import com.yogpc.qp.utils.Holder
import com.yogpc.qp.{QuarryPlus, _}
import mezz.jei.api.constants.VanillaTypes
import mezz.jei.api.gui.IRecipeLayout
import mezz.jei.api.gui.drawable.IDrawableAnimated.StartDirection
import mezz.jei.api.gui.drawable.{IDrawable, IDrawableAnimated, IDrawableStatic}
import mezz.jei.api.helpers.IGuiHelper
import mezz.jei.api.ingredients.IIngredients
import mezz.jei.api.recipe.category.IRecipeCategory
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.I18n
import net.minecraft.enchantment.{EnchantmentData, EnchantmentType}
import net.minecraft.item.{EnchantedBookItem, Item, ItemStack, Items}
import net.minecraft.util.ResourceLocation
import net.minecraftforge.registries.ForgeRegistries

import scala.jdk.CollectionConverters._

class BookRecipeCategory(guiHelper: IGuiHelper) extends IRecipeCategory[BookRecipeCategory.BookRecipe] {

  import BookRecipeCategory._

  val bar: IDrawableStatic = guiHelper.createDrawable(backGround, 176, 14, 23, 16)
  val animateBar: IDrawableAnimated = guiHelper.createAnimatedDrawable(bar, 100, StartDirection.LEFT, false)

  override def getUid: ResourceLocation = UID

  override def getTitle: String = I18n.format(Holder.blockBookMover.getTranslationKey)

  override def getBackground: IDrawable = guiHelper.createDrawable(backGround, xOff, yOff, 167, 77)

  override def setRecipe(recipeLayout: IRecipeLayout, recipeWrapper: BookRecipe, ingredients: IIngredients): Unit = {
    val guiItemStack = recipeLayout.getItemStacks

    guiItemStack.init(0, true, 8, 30)
    guiItemStack.init(1, true, 50, 30)
    guiItemStack.init(2, false, 111, 30)

    guiItemStack.set(ingredients)
  }

  override def draw(recipe: BookRecipe, mouseX: Double, mouseY: Double): Unit = {
    super.draw(recipe, mouseX, mouseY)
    animateBar.draw(75, 31)
    Minecraft.getInstance().fontRenderer.drawString("50000MJ", 36 - xOff, 66 - yOff, 0x404040)
  }

  override def getRecipeClass: Class[_ <: BookRecipe] = classOf[BookRecipe]

  override def getIcon: IDrawable = guiHelper.createDrawableIngredient(new ItemStack(Holder.blockBookMover))

  override def setIngredients(recipe: BookRecipe, ingredients: IIngredients): Unit = {
    val input = Seq(items.asJava, Collections.singletonList(EnchantedBookItem.getEnchantedItemStack(recipe.ench))).asJava
    val output = items.map(_.copy()).map(_.enchantmentAdded(recipe.ench.enchantment, recipe.ench.enchantmentLevel)).asJava

    ingredients.setInputLists(VanillaTypes.ITEM, input)
    ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(output))
  }
}

object BookRecipeCategory {
  final val UID = new ResourceLocation(QuarryPlus.modID, "quarryplus.bookmover")
  val backGround = new ResourceLocation(QuarryPlus.modID, "textures/gui/bookmover_jei.png")
  final val xOff = 0
  final val yOff = 0
  final val o = 18

  val enchTypes: Set[EnchantmentType] = EnchantmentType.values().filter(_.canEnchantItem(Items.DIAMOND_PICKAXE)).toSet
  val enchantments: Iterable[EnchantmentData] = ForgeRegistries.ENCHANTMENTS.getValues.asScala.filter(e => enchTypes(e.`type`)).map(e => new EnchantmentData(e, e.getMaxLevel))
  val items: Seq[ItemStack] = ForgeRegistries.ITEMS.asScala.collect { case i: Item with IEnchantableItem if i.isValidInBookMover => i }.flatMap(_.stacks).toSeq

  def recipes: java.util.List[BookRecipe] = enchantments.map(BookRecipe.apply).toSeq.asJava

  case class BookRecipe(ench: EnchantmentData)

}
*/