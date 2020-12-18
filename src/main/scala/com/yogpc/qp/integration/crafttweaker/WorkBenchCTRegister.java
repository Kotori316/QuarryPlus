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

    /**
     * Add a recipe of ONE input and output.
     * Example <blockquote><pre>
     * WorkbenchPlus.INSTANCE.addSingleInputRecipe(
     *     "convert_glass", // recipe name
     *     &lt;tag:items:forge:stained_glass&gt;, // input
     *     &lt;item:minecraft:glass&gt;, // output
     *     1000 // energy
     * );
     * </pre></blockquote>
     *
     * @param recipeName the name of recipe. [a-z0-9/._-] is allowed.
     * @param input      the input ingredient.
     * @param output     the output item.
     * @param energy     required energy. Unit [FE]
     */
    @ZenCodeType.Method
    public void addSingleInputRecipe(String recipeName, IIngredient input, IItemStack output, @ZenCodeType.OptionalFloat(1000) float energy) {
        applyRecipeAddAction(CTRecipe.createWorkbenchRecipe(validateRecipeName(recipeName), output, input, energy));
    }

    /**
     * Add a recipe of multi inputs and one output.
     * Example <blockquote><pre>
     * WorkbenchPlus.INSTANCE.addMultiInputRecipe(
     *    "get_more_blaze_powder", // recipe name
     *    [&lt;item:minecraft:blaze_rod&gt;, &lt;item:minecraft:magma_cream&gt;], // inputs
     *    &lt;item:minecraft:blaze_powder&gt; * 5, // output
     *    1000 // energy
     * );
     * </pre></blockquote>
     *
     * @param recipeName the name of recipe. [a-z0-9/._-] is allowed.
     * @param inputs     array of the input ingredients.
     * @param output     the output item.
     * @param energy     required energy. Unit [FE]
     */
    @ZenCodeType.Method
    public void addMultiInputRecipe(String recipeName, IIngredient[] inputs, IItemStack output, @ZenCodeType.OptionalFloat(1000) float energy) {
        applyRecipeAddAction(CTRecipe.createWorkbenchRecipe(validateRecipeName(recipeName), output, inputs, energy));
    }

    /**
     * Add a recipe of multi inputs and one output.
     * Example <blockquote><pre>
     * var inputs = [
     *   [&lt;item:minecraft:coal&gt, &lt;item:minecraft:charcoal&gt] as IIngredient[],
     *   [&lt;tag:items:minecraft:logs&gt;] as IIngredient[]
     * ] as IIngredient[][];
     * WorkbenchPlus.INSTANCE.addRecipe(
     *    "get_more_torch", // recipe name
     *    inputs,
     *    &lt;item:minecraft:torch&gt; * 64, // output
     *    1000 // energy
     * );
     * </pre></blockquote>
     *
     * @param recipeName the name of recipe. [a-z0-9/._-] is allowed.
     * @param inputs     array of the input ingredients.
     * @param output     the output item.
     * @param energy     required energy. Unit [FE]
     */
    @ZenCodeType.Method
    public void addRecipe(String recipeName, IIngredient[][] inputs, IItemStack output, @ZenCodeType.OptionalFloat(1000) float energy) {
        applyRecipeAddAction(CTRecipe.createWorkbenchRecipe(validateRecipeName(recipeName), output, inputs, energy));
    }

    /**
     * @param recipeName  the name of recipe. [a-z0-9/._-] is allowed.
     * @param copyFrom    the item from which nbt is copied.
     * @param otherInputs other input item. Empty([]) is allowed if additional input is nothing.
     * @param output      the item to which nbt is moved
     * @param energy      required energy. Unit [FE]
     */
    @ZenCodeType.Method
    public void addEnchantmentCopyRecipe(String recipeName, IIngredient[] copyFrom, IIngredient[][] otherInputs, IItemStack output, @ZenCodeType.OptionalFloat(1000) float energy) {
        applyRecipeAddAction(CTRecipe.createEnchantmentCopyRecipe(validateRecipeName(recipeName), output, copyFrom, otherInputs, energy));
    }
}
