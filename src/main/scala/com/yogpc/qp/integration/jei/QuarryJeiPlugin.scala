package com.yogpc.qp.integration.jei

import com.yogpc.qp.QuarryPlusI
import com.yogpc.qp.gui.GuiWorkbench
import com.yogpc.qp.tile.WorkbenchRecipes
import mezz.jei.api.ingredients.IModIngredientRegistration
import mezz.jei.api.recipe.{IRecipeCategoryRegistration, IRecipeWrapperFactory}
import mezz.jei.api.{BlankModPlugin, IJeiRuntime, IModRegistry, ISubtypeRegistry, JEIPlugin}
import net.minecraft.item.ItemStack

@JEIPlugin
class QuarryJeiPlugin extends BlankModPlugin {

    //noinspection ConvertExpressionToSAM
    override def register(registry: IModRegistry): Unit = {
        registry.handleRecipes(classOf[WorkbenchRecipes], new IRecipeWrapperFactory[WorkbenchRecipes] {
            override def getRecipeWrapper(recipe: WorkbenchRecipes) = new WorkBenchRecipeWrapper(recipe)
        }, WorkBenchRecipeCategory.UID)
        registry.addRecipeCatalyst(new ItemStack(QuarryPlusI.blockWorkbench), WorkBenchRecipeCategory.UID)
        // 7, 74 => 168, 85
        registry.addRecipeClickArea(classOf[GuiWorkbench], 7, 74, 161, 11, WorkBenchRecipeCategory.UID)
        registry.addRecipes(WorkBenchRecipeWrapper.getAll, WorkBenchRecipeCategory.UID)
    }

    override def registerIngredients(registry: IModIngredientRegistration): Unit = super.registerIngredients(registry)

    override def onRuntimeAvailable(jeiRuntime: IJeiRuntime): Unit = super.onRuntimeAvailable(jeiRuntime)

    override def registerItemSubtypes(subtypeRegistry: ISubtypeRegistry): Unit = super.registerItemSubtypes(subtypeRegistry)

    override def registerCategories(registry: IRecipeCategoryRegistration): Unit = {
        super.registerCategories(registry)
        registry.addRecipeCategories(new WorkBenchRecipeCategory(registry.getJeiHelpers.getGuiHelper))
    }

}
