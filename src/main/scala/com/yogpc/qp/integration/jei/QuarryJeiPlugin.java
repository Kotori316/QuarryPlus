package com.yogpc.qp.integration.jei;

import java.util.List;
import java.util.stream.Collectors;

import com.yogpc.qp.Config;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.bookmover.BlockBookMover;
import com.yogpc.qp.machines.bookmover.GuiBookMover;
import com.yogpc.qp.machines.workbench.GuiWorkbench;
import com.yogpc.qp.machines.workbench.WorkbenchRecipes;
import com.yogpc.qp.utils.Holder;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import scala.collection.JavaConverters;

@JeiPlugin
public class QuarryJeiPlugin implements IModPlugin {
    static IJeiRuntime jeiRuntime;
    static WorkBenchRecipeCategory workBenchRecipeCategory;
    static MoverRecipeCategory moverRecipeCategory;
    static BookRecipeCategory bookRecipeCategory;

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<WorkbenchRecipes> recipes = JavaConverters.mapAsJavaMap(WorkbenchRecipes.recipes()).values().stream()
            .filter(WorkbenchRecipes::showInJEI)
            .sorted(WorkbenchRecipes.recipeOrdering())
            .collect(Collectors.toList());
        registration.addRecipes(recipes, WorkBenchRecipeCategory.UID());
        registration.addRecipes(BookRecipeCategory.recipes(), BookRecipeCategory.UID());
        registration.addRecipes(MoverRecipeCategory.recipes(), MoverRecipeCategory.UID());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(Holder.blockWorkbench()), WorkBenchRecipeCategory.UID());
        registration.addRecipeCatalyst(new ItemStack(Holder.blockBookMover()), BookRecipeCategory.UID());
        registration.addRecipeCatalyst(new ItemStack(Holder.blockMover()), MoverRecipeCategory.UID());
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(GuiWorkbench.class, 7, 74, 161, 11, WorkBenchRecipeCategory.UID());
        registration.addRecipeClickArea(GuiBookMover.class, 79, 35, 23, 16, BookRecipeCategory.UID());
    }

//    @Override
//    public void register(IModRegistry registry) {
//        registry.handleRecipes(MoverRecipeWrapper.MoverRecipe.class, MoverRecipeWrapper::new, MoverRecipeCategory.UID());
//        registry.addRecipeCatalyst(new ItemStack(QuarryPlusI.blockMover()), MoverRecipeCategory.UID());
//        registry.addRecipes(MoverRecipeWrapper.recipes(), MoverRecipeCategory.UID());
//
//        if (!Config.content().disableMapJ().get(BlockBookMover.SYMBOL)) {
//            registry.handleRecipes(BookRecipeWrapper.BookRecipe.class, BookRecipeWrapper::new, BookRecipeCategory.UID());
//            registry.addRecipeCatalyst(new ItemStack(QuarryPlusI.blockBookMover()), BookRecipeCategory.UID());
//            registry.addRecipeClickArea(GuiBookMover.class, 79, 35, 23, 16, BookRecipeCategory.UID());
//            registry.addRecipes(BookRecipeWrapper.recipes(), BookRecipeCategory.UID());
//        }
//
//        MinecraftForge.EVENT_BUS.register(this);
//    }

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(QuarryPlus.modID, "jei_recipe");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        workBenchRecipeCategory = new WorkBenchRecipeCategory(registry.getJeiHelpers().getGuiHelper());
        registry.addRecipeCategories(workBenchRecipeCategory);
        moverRecipeCategory = new MoverRecipeCategory(registry.getJeiHelpers().getGuiHelper());
        registry.addRecipeCategories(moverRecipeCategory);
        if (!Config.common().disabled().apply(BlockBookMover.SYMBOL).get()) {
            bookRecipeCategory = new BookRecipeCategory(registry.getJeiHelpers().getGuiHelper());
            registry.addRecipeCategories(bookRecipeCategory);
        }
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        QuarryJeiPlugin.jeiRuntime = jeiRuntime;
    }
}
