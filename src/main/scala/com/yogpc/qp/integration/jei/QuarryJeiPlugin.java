package com.yogpc.qp.integration.jei;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.workbench.RecipeFinder;
import com.yogpc.qp.machines.workbench.ScreenWorkbench;
import com.yogpc.qp.machines.workbench.WorkbenchRecipe;
import me.shedaniel.rei.plugincompatibilities.api.REIPluginCompatIgnore;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
@REIPluginCompatIgnore
public class QuarryJeiPlugin implements IModPlugin {
    static IJeiRuntime jeiRuntime;
    static WorkBenchRecipeCategory workBenchRecipeCategory;
    static MoverRecipeCategory moverRecipeCategory;

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<WorkbenchRecipe> recipes =
            RecipeFinder.find(Objects.requireNonNull(Minecraft.getInstance().level).getRecipeManager(), WorkbenchRecipe.RECIPE_TYPE).values().stream() // Synced by server.
                .filter(WorkbenchRecipe::showInJEI)
                .filter(WorkbenchRecipe::hasContent)
                .sorted(WorkbenchRecipe.COMPARATOR)
                .collect(Collectors.toList());
        registration.addRecipes(WorkBenchRecipeCategory.RECIPE_TYPE, recipes);
        registration.addRecipes(MoverRecipeCategory.RECIPE_TYPE, MoverRecipeCategory.recipes());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(Holder.BLOCK_WORKBENCH), WorkBenchRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(Holder.BLOCK_MOVER), MoverRecipeCategory.RECIPE_TYPE);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(ScreenWorkbench.class, 7, 74, 161, 11, WorkBenchRecipeCategory.RECIPE_TYPE);
    }

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
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        QuarryJeiPlugin.jeiRuntime = jeiRuntime;
    }
}
