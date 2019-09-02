package com.yogpc.qp.integration.jei

import java.util.{Collections, ArrayList => AList, List => JList}

import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.integration.jei.WorkBenchRecipeCategory._
import com.yogpc.qp.machines.base.APowerTile
import com.yogpc.qp.machines.workbench.WorkbenchRecipes
import com.yogpc.qp.utils.Holder
import mezz.jei.api.constants.VanillaTypes
import mezz.jei.api.gui.IRecipeLayout
import mezz.jei.api.gui.drawable.IDrawable
import mezz.jei.api.gui.drawable.IDrawableAnimated.StartDirection
import mezz.jei.api.helpers.IGuiHelper
import mezz.jei.api.ingredients.IIngredients
import mezz.jei.api.recipe.category.IRecipeCategory
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.I18n
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation

import scala.collection.JavaConverters._

class WorkBenchRecipeCategory(guiHelper: IGuiHelper) extends IRecipeCategory[WorkbenchRecipes] {
  //4, 13 => 175, 94

  val bar = guiHelper.createDrawable(backGround, xOff, 87, 160, 4)
  val animateBar = guiHelper.createAnimatedDrawable(bar, 300, StartDirection.LEFT, false)

  override val getBackground: IDrawable = guiHelper.createDrawable(backGround, xOff, yOff, 167, 86)

  override def setRecipe(recipeLayout: IRecipeLayout, recipeWrapper: WorkbenchRecipes, ingredients: IIngredients): Unit = {
    val guiItemStack = recipeLayout.getItemStacks
    //7, 17 -- 7, 89

    val x0 = 3
    for (i <- 0 until recipeWrapper.size) {
      if (i < 9) {
        guiItemStack.init(i, true, x0 + o * i - xOff, x0 - yOff)
      } else if (i < 18) {
        guiItemStack.init(i, true, x0 + o * (i - 9) - xOff, x0 + o - yOff)
      }
    }
    guiItemStack.init(recipeWrapper.size, false, x0 - xOff, x0 + 64 - yOff)
    guiItemStack.set(ingredients)
  }

  override def getTitle: String = I18n.format(Holder.blockWorkbench.getTranslationKey)

  override val getUid = UID

  override def draw(recipe: WorkbenchRecipes, mouseX: Double, mouseY: Double): Unit = {
    animateBar.draw(4, 60)
    Minecraft.getInstance().fontRenderer.drawString((recipe.energy.toDouble / APowerTile.MJToMicroMJ).toString + "MJ", 36 - xOff, 70 - yOff, 0x404040)
  }

  override def getRecipeClass = classOf[WorkbenchRecipes]

  override val getIcon = guiHelper.createDrawableIngredient(new ItemStack(Holder.blockWorkbench))

  override def setIngredients(recipe: WorkbenchRecipes, ingredients: IIngredients): Unit = {
    val inputs = new AList[JList[ItemStack]](recipe.size)

    recipe.inputs.foreach(l => inputs add l.flatMap(_.stackList).asJava)
    val outputs = Collections.singletonList(recipe.getOutput)

    ingredients.setInputLists(VanillaTypes.ITEM, inputs)
    ingredients.setOutputs(VanillaTypes.ITEM, outputs)
  }
}

object WorkBenchRecipeCategory {
  final val UID = new ResourceLocation(QuarryPlus.modID, "jei_workbenchplus")
  val backGround = new ResourceLocation(QuarryPlus.modID, "textures/gui/workbench_jei2.png")
  final val xOff = 0
  final val yOff = 0
  final val o = 18
}
