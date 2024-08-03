package com.yogpc.qp.gametest;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class LoadRecipeTest {
    public static void getRecipes(GameTestHelper helper) {
        var manager = helper.getLevel().getRecipeManager();
        var recipes = manager.getRecipes().stream()
            .filter(h -> h.id().getNamespace().equals(QuarryPlus.modID))
            .toList();

        assertFalse(recipes.isEmpty());
        helper.succeed();
    }

    public static void createMarker(GameTestHelper helper) {
        var manager = helper.getLevel().getRecipeManager();
        var recipeOptional = manager.byKey(ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, "marker"));
        assertTrue(recipeOptional.isPresent());
        var recipe = (CraftingRecipe) recipeOptional.get().value();

        var g = Items.GLOWSTONE_DUST.getDefaultInstance();
        var l = Items.LAPIS_LAZULI.getDefaultInstance();
        var r = Items.REDSTONE_TORCH.getDefaultInstance();
        var input = CraftingInput.of(3, 3, List.of(
            g, l, g,
            l, r, l,
            g, l, ItemStack.EMPTY
        ));
        var result = recipe.matches(input, helper.getLevel());
        assertTrue(result);
        helper.succeed();
    }
}
