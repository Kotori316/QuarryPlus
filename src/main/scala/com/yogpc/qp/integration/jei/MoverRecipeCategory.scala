package com.yogpc.qp.integration.jei

import java.util.Collections

import com.mojang.blaze3d.matrix.MatrixStack
import com.yogpc.qp.machines.base.IEnchantableItem
import com.yogpc.qp.utils.Holder
import com.yogpc.qp.{QuarryPlus, _}
import mezz.jei.api.constants.VanillaTypes
import mezz.jei.api.gui.IRecipeLayout
import mezz.jei.api.gui.drawable.IDrawable
import mezz.jei.api.helpers.IGuiHelper
import mezz.jei.api.ingredients.IIngredients
import mezz.jei.api.recipe.category.IRecipeCategory
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.I18n
import net.minecraft.enchantment.Enchantment
import net.minecraft.item.{Item, ItemStack, Items}
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.TranslationTextComponent
import net.minecraftforge.registries.ForgeRegistries

import scala.jdk.CollectionConverters._

class MoverRecipeCategory(guiHelper: IGuiHelper) extends IRecipeCategory[MoverRecipeCategory.MoverRecipe] {

  import MoverRecipeCategory._

  override def getUid: ResourceLocation = UID

  override def getTitle: String = I18n.format(Holder.blockMover.getTranslationKey)

  override def getBackground: IDrawable = guiHelper.createDrawable(backGround, xOff, yOff, 167, 76)

  override def setRecipe(recipeLayout: IRecipeLayout, recipeWrapper: MoverRecipe, ingredients: IIngredients): Unit = {
    val guiItemStack = recipeLayout.getItemStacks

    guiItemStack.init(0, true, 3, 30)
    guiItemStack.init(1, false, 3 + 144, 30)

    guiItemStack.set(ingredients)
  }

  override def getRecipeClass: Class[_ <: MoverRecipe] = classOf[MoverRecipe]

  override def getIcon: IDrawable = guiHelper.createDrawableIngredient(new ItemStack(Holder.blockMover))

  override def setIngredients(recipe: MoverRecipe, ingredients: IIngredients): Unit = {
    val input = recipe.enchantments.flatMap(e => pickaxes.map(i => e -> i.enchantmentAdded(e, e.getMaxLevel)))
    val output = input.map { case (e, _) => recipe.toStack.enchantmentAdded(e, e.getMaxLevel) }

    ingredients.setInputLists(VanillaTypes.ITEM, Collections.singletonList(input.map(_._2).asJava))
    ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(output.asJava))
  }

  override def draw(recipe: MoverRecipe, matrixStack: MatrixStack, mouseX: Double, mouseY: Double): Unit = {
    super.draw(recipe, matrixStack, mouseX, mouseY)
    recipe.enchantments.zipWithIndex.foreach { case (e, i) =>
      Minecraft.getInstance().fontRenderer.func_238422_b_(matrixStack, new TranslationTextComponent(e.getName).func_241878_f(), (36 - xOff).toFloat, (6 - yOff + 10 * i).toFloat, 0x404040)
    }
  }
}

object MoverRecipeCategory {
  final val UID = new ResourceLocation(QuarryPlus.modID, "quarryplus.enchantmover")
  val backGround = new ResourceLocation(QuarryPlus.modID, "textures/gui/mover_jei.png")
  final val xOff = 0
  final val yOff = 0
  final val o = 18

  def recipes: java.util.Collection[MoverRecipe] =
    ForgeRegistries.ITEMS.asScala
      .collect { case i: Item with IEnchantableItem => i }
      .flatMap(item => item.stacks().map(s => MoverRecipe(item, s)))
      .toSeq.asJava

  case class MoverRecipe(item: Item with IEnchantableItem, stack: ItemStack) extends (Enchantment => Boolean) {

    import net.minecraft.enchantment.Enchantments._

    val enchantments: Seq[Enchantment] = Seq(EFFICIENCY, UNBREAKING, FORTUNE, SILK_TOUCH).filter(this)

    def toStack: ItemStack = stack.copy()

    def canMove(enchantment: Enchantment): Boolean = item.canMove(toStack, enchantment)

    override def apply(v1: Enchantment): Boolean = canMove(v1)

    override def toString(): String = s"MoverRecipe{$item, $enchantments}"
  }

  def pickaxes = List(new ItemStack(Items.DIAMOND_PICKAXE), new ItemStack(Items.NETHERITE_PICKAXE))
}
