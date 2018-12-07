package com.yogpc.qp.integration.jei;

import com.yogpc.qp.Config;
import com.yogpc.qp.QuarryPlusI;
import com.yogpc.qp.block.BlockBookMover;
import com.yogpc.qp.gui.GuiBookMover;
import com.yogpc.qp.gui.GuiWorkbench;
import com.yogpc.qp.tile.WorkbenchRecipes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
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

        registry.handleRecipes(MoverRecipeWrapper.MoverRecipe.class, MoverRecipeWrapper::new, MoverRecipeCategory.UID());
        registry.addRecipeCatalyst(new ItemStack(QuarryPlusI.blockMover()), MoverRecipeCategory.UID());
        registry.addRecipes(MoverRecipeWrapper.recipes(), MoverRecipeCategory.UID());

        if (!Config.content().disableMapJ().get(BlockBookMover.SYMBOL)) {
            registry.handleRecipes(BookRecipeWrapper.BookRecipe.class, BookRecipeWrapper::new, BookRecipeCategory.UID());
            registry.addRecipeCatalyst(new ItemStack(QuarryPlusI.blockBookMover()), BookRecipeCategory.UID());
            registry.addRecipeClickArea(GuiBookMover.class, 79, 35, 23, 16, BookRecipeCategory.UID());
            registry.addRecipes(BookRecipeWrapper.recipes(), BookRecipeCategory.UID());
        }
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(new WorkBenchRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
        registry.addRecipeCategories(new MoverRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
        if (!Config.content().disableMapJ().get(BlockBookMover.SYMBOL))
            registry.addRecipeCategories(new BookRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
    }

}
