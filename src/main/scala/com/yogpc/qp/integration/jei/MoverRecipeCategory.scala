package com.yogpc.qp.integration.jei

import com.yogpc.qp.QuarryPlus
import net.minecraft.util.ResourceLocation

/*
class MoverRecipeCategory(guiHelper: IGuiHelper) extends IRecipeCategory[MoverRecipeWrapper] {

  import MoverRecipeCategory._

  override def getUid: String = UID

  override def getTitle: String = QuarryPlusI.blockMover.getLocalizedName

  override def getModName: String = QuarryPlus.Mod_Name

  override def getBackground: IDrawable = guiHelper.createDrawable(backGround, xOff, yOff, 167, 76)

  override def setRecipe(recipeLayout: IRecipeLayout, recipeWrapper: MoverRecipeWrapper, ingredients: IIngredients): Unit = {
    val guiItemStack = recipeLayout.getItemStacks

    guiItemStack.init(0, true, 3, 30)
    guiItemStack.init(1, false, 3 + 144, 30)

    guiItemStack.set(ingredients)
  }
}
*/
object MoverRecipeCategory {
  final val UID = "quarryplus.enchantmover"
  val backGround = new ResourceLocation(QuarryPlus.modID, "textures/gui/mover_jei.png")
  final val xOff = 0
  final val yOff = 0
  final val o = 18
}
