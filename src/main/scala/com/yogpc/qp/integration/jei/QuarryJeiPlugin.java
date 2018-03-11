package com.yogpc.qp.integration.jei;

import com.yogpc.qp.QuarryPlusI;
import com.yogpc.qp.gui.GuiWorkbench;
import com.yogpc.qp.tile.WorkbenchRecipes;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.item.ItemStack;

@JEIPlugin
public class QuarryJeiPlugin implements IModPlugin {

    @Override
    public void register(IModRegistry registry) {
        registry.handleRecipes(WorkbenchRecipes.class, WorkBenchRecipeWrapper::new, WorkBenchRecipeCategory.UID());
        registry.addRecipeCatalyst(new ItemStack(QuarryPlusI.blockWorkbench()), WorkBenchRecipeCategory.UID());
        registry.addRecipeClickArea(GuiWorkbench.class, 7, 74, 161, 11, WorkBenchRecipeCategory.UID());
        registry.addRecipes(WorkBenchRecipeWrapper.getAll(), WorkBenchRecipeCategory.UID());
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {

    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {

    }

    @Override
    public void registerIngredients(IModIngredientRegistration registry) {

    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(new WorkBenchRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
    }

}
