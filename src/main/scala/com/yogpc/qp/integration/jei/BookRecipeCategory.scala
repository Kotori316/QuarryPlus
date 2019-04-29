package com.yogpc.qp.integration.jei

import com.yogpc.qp.QuarryPlus
import net.minecraft.util.ResourceLocation

/*
class BookRecipeCategory(guiHelper: IGuiHelper) extends IRecipeCategory[BookRecipeWrapper] {

  import BookRecipeCategory._

  val bar = guiHelper.createDrawable(backGround, 176, 14, 23, 16)
  val animateBar = guiHelper.createAnimatedDrawable(bar, 100, StartDirection.LEFT, false)

  override def getUid: String = UID

  override def getTitle: String = QuarryPlusI.blockBookMover.getLocalizedName

  override def getModName: String = QuarryPlus.Mod_Name

  override def getBackground: IDrawable = guiHelper.createDrawable(backGround, xOff, yOff, 167, 77)

  override def setRecipe(recipeLayout: IRecipeLayout, recipeWrapper: BookRecipeWrapper, ingredients: IIngredients): Unit = {
    val guiItemStack = recipeLayout.getItemStacks

    guiItemStack.init(0, true, 8, 30)
    guiItemStack.init(1, true, 50, 30)
    guiItemStack.init(2, false, 111, 30)

    guiItemStack.set(ingredients)
  }

  override def drawExtras(minecraft: Minecraft): Unit = {
    animateBar.draw(minecraft, 75, 31)

}
}*/
object BookRecipeCategory {
  final val UID = "quarryplus.bookmover"
  val backGround = new ResourceLocation(QuarryPlus.modID, "textures/gui/bookmover_jei.png")
  final val xOff = 0
  final val yOff = 0
  final val o = 18
}
