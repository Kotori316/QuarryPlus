package com.yogpc.qp.utils;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class RecipeGetter {
    private static final Method getRecipes;

    static {
        Method getRecipesMethod;
        try {
            /*
             * The real signature is private <C extends IInventory, T extends IRecipe<C>> Map<ResourceLocation, IRecipe<C>> getRecipes(IRecipeType<T> recipeTypeIn)
             */
            getRecipesMethod = ObfuscationReflectionHelper.findMethod(RecipeManager.class, "func_215366_a", IRecipeType.class);
        } catch (Exception e) {
            e.printStackTrace();
            getRecipesMethod = null;
        }
        getRecipes = getRecipesMethod;
    }

    @SuppressWarnings({"unchecked", "UnnecessaryLocalVariable"})
    public static <T extends IRecipe<?>> Map<ResourceLocation, T> getRecipes(RecipeManager manager, IRecipeType<T> recipeTypeIn) {
        if (getRecipes == null) {
            return Collections.emptyMap();
        } else {
            try {
                Map<ResourceLocation, T> invoke = (Map<ResourceLocation, T>) getRecipes.invoke(manager, recipeTypeIn);
                return invoke;
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
                return Collections.emptyMap();
            }
        }
    }
}
