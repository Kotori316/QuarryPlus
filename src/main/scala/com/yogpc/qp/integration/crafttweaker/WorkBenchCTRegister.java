package com.yogpc.qp.integration.crafttweaker;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.impl.actions.recipes.ActionAddRecipe;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.workbench.WorkbenchRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(WorkBenchCTRegister.packageName)
public class WorkBenchCTRegister implements IRecipeManager {
    public static final String packageName = "mods." + QuarryPlus.modID + ".WorkbenchPlus";

    @ZenCodeType.Field
    public static final WorkBenchCTRegister INSTANCE = new WorkBenchCTRegister();

    private WorkBenchCTRegister() {
    }

    @SuppressWarnings("rawtypes")
    @Override
    public IRecipeType getRecipeType() {
        return WorkbenchRecipes.recipeType();
    }

    private void applyRecipeAddAction(IRecipe<?> recipe) {
        CraftTweakerAPI.apply(new ActionAddRecipe(this, recipe, ""));
    }

    @ZenCodeType.Method
    public void addSingleInputRecipe(String recipeName, IIngredient input, IItemStack output, @ZenCodeType.OptionalFloat(1000) float energy) {
        applyRecipeAddAction(CTRecipe.createWorkbenchRecipe(fixRecipeName(recipeName), output, input, energy));
    }

    @ZenCodeType.Method
    public void addMultiInputRecipe(String recipeName, IIngredient[] inputs, IItemStack output, @ZenCodeType.OptionalFloat(1000) float energy) {
        applyRecipeAddAction(CTRecipe.createWorkbenchRecipe(fixRecipeName(recipeName), output, inputs, energy));
    }

    @ZenCodeType.Method
    public void addRecipe(String recipeName, IIngredient[][] inputs, IItemStack output, @ZenCodeType.OptionalFloat(1000) float energy) {
        applyRecipeAddAction(CTRecipe.createWorkbenchRecipe(fixRecipeName(recipeName), output, inputs, energy));
    }

    @ZenCodeType.Method
    public void addEnchantmentCopyRecipe(String recipeName, IIngredient[] copyFrom, IIngredient[][] otherInputs, IItemStack output, @ZenCodeType.OptionalFloat(1000) float energy) {
        applyRecipeAddAction(CTRecipe.createEnchantmentCopyRecipe(fixRecipeName(recipeName), output, copyFrom, otherInputs, energy));
    }
}
