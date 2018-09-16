package com.yogpc.qp.integration.jei

import com.yogpc.qp.integration.jei.WorkBenchRecipeCategory._
import com.yogpc.qp.{QuarryPlus, QuarryPlusI}
import mezz.jei.api.IGuiHelper
import mezz.jei.api.gui.{IDrawable, IRecipeLayout}
import mezz.jei.api.ingredients.IIngredients
import mezz.jei.api.recipe.IRecipeCategory
import net.minecraft.client.Minecraft
import net.minecraft.util.ResourceLocation

class WorkBenchRecipeCategory(guiHelper: IGuiHelper) extends IRecipeCategory[WorkBenchRecipeWrapper] {
    //4, 13 => 172, 110
    private[this] final val xOff = 4
    private[this] final val yOff = 13
    private[this] final val o = 18
    private[this] var currentrecipe: WorkBenchRecipeWrapper = _

    override val getBackground: IDrawable = guiHelper.createDrawable(backGround, xOff, yOff, 168, 98)

    override def setRecipe(recipeLayout: IRecipeLayout, recipeWrapper: WorkBenchRecipeWrapper, ingredients: IIngredients): Unit = {
        val guiItemStack = recipeLayout.getItemStacks
        //7, 17 -- 7, 89
        for (i <- 0 until recipeWrapper.recipeSize) {
            if (i < 9) {
                guiItemStack.init(i, true, 7 + o * i - xOff, 17 - yOff)
            } else if (i < 18) {
                guiItemStack.init(i, true, 7 + o * (i - 9) - xOff, 17 + o - yOff)
            }
        }
        guiItemStack.init(recipeWrapper.recipeSize, false, 7 - xOff, 89 - yOff)
        guiItemStack.set(ingredients)
        currentrecipe = recipeWrapper
    }

    override def getTitle: String = QuarryPlusI.blockWorkbench.getLocalizedName

    override val getUid: String = UID

    override def drawExtras(minecraft: Minecraft): Unit = {
        super.drawExtras(minecraft)
        if (currentrecipe != null) {
            minecraft.fontRenderer.drawString(currentrecipe.getEnergyRequired.toString + "MJ", 40 - xOff, 90 - yOff, 0x404040)
        }
    }

    override def getModName: String = QuarryPlus.Mod_Name
}

object WorkBenchRecipeCategory {
    final val UID = "quarryplus.workbenchplus"
    val backGround = new ResourceLocation(QuarryPlus.modID, "textures/gui/workbench_jei2.png")
}