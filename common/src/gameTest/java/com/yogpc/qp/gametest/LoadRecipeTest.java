package com.yogpc.qp.gametest;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"unused", "DuplicatedCode"})
public final class LoadRecipeTest {
    public static void getRecipes(GameTestHelper helper) {
        var manager = helper.getLevel().getRecipeManager();
        var recipes = manager.getRecipes().stream()
            .filter(h -> h.id().getNamespace().equals(QuarryPlus.modID))
            .toList();

        assertFalse(recipes.isEmpty());
        helper.succeed();
    }

    private static CraftingRecipe findRecipe(GameTestHelper helper, String id) {
        var manager = helper.getLevel().getRecipeManager();
        var holder = manager.byKey(ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, id));
        if (holder.isEmpty()) {
            throw new AssertionError("Recipe %s is not found".formatted(id));
        }
        var recipe = holder
            .map(RecipeHolder::value)
            .filter(CraftingRecipe.class::isInstance)
            .map(CraftingRecipe.class::cast)
            .orElse(null);

        if (recipe == null) {
            throw new AssertionError("Recipe %s is not found".formatted(id));
        }
        return recipe;
    }

    private static CraftingRecipe findRecipeNullable(GameTestHelper helper, String id) {
        try {
            return findRecipe(helper, id);
        } catch (AssertionError e) {
            return null;
        }
    }

    public static void createMarker(GameTestHelper helper) {
        var recipe = findRecipe(helper, "marker");

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

    public static void createQuarry(GameTestHelper helper) {
        var recipe = findRecipe(helper, "quarry");

        var D = Items.DROPPER.getDefaultInstance();
        var R = Items.REDSTONE_BLOCK.getDefaultInstance();
        var g = Items.GOLDEN_PICKAXE.getDefaultInstance();
        var i = Items.IRON_INGOT.getDefaultInstance();
        var o = Items.OBSIDIAN.getDefaultInstance();
        var m = PlatformAccess.getAccess().registerObjects().markerBlock().get().blockItem.getDefaultInstance();

        var input = CraftingInput.of(3, 3, List.of(
            i, o, i,
            g, D, g,
            i, R, m
        ));
        assertTrue(recipe.matches(input, helper.getLevel()));
        var result = recipe.assemble(input, helper.getLevel().registryAccess());
        assertEquals(
            PlatformAccess.getAccess().registerObjects().quarryBlock().get().blockItem,
            result.getItem()
        );

        helper.succeed();
    }

    public static void createStatusChecker(GameTestHelper helper) {
        var recipe = findRecipe(helper, "status_checker");

        var g = Items.GLASS.getDefaultInstance();
        var l = Items.IRON_INGOT.getDefaultInstance();
        var r = PlatformAccess.getAccess().registerObjects().markerBlock().get().blockItem.getDefaultInstance();

        var input = CraftingInput.of(3, 2, List.of(
            g, g, g,
            l, r, l
        ));
        assertTrue(recipe.matches(input, helper.getLevel()));
        var result = recipe.assemble(input, helper.getLevel().registryAccess());
        assertEquals("status_checker", BuiltInRegistries.ITEM.getKey(result.getItem()).getPath());

        helper.succeed();
    }


    public static void createYSetter(GameTestHelper helper) {
        var recipe = findRecipe(helper, "y_setter");

        var g = Items.GLASS.getDefaultInstance();
        var l = Items.LAPIS_LAZULI.getDefaultInstance();
        var r = PlatformAccess.getAccess().registerObjects().markerBlock().get().blockItem.getDefaultInstance();

        var input = CraftingInput.of(3, 2, List.of(
            g, g, g,
            l, r, l
        ));
        assertTrue(recipe.matches(input, helper.getLevel()));
        var result = recipe.assemble(input, helper.getLevel().registryAccess());
        assertEquals("y_setter", BuiltInRegistries.ITEM.getKey(result.getItem()).getPath());

        helper.succeed();
    }

    public static void createMover(GameTestHelper helper) {
        var recipe = findRecipe(helper, "mover");

        var d = Items.DIAMOND.getDefaultInstance();
        var a = Items.ANVIL.getDefaultInstance();
        var g = Items.GOLD_INGOT.getDefaultInstance();
        var i = Items.IRON_INGOT.getDefaultInstance();
        var o = Items.OBSIDIAN.getDefaultInstance();
        var m = PlatformAccess.getAccess().registerObjects().markerBlock().get().blockItem.getDefaultInstance();
        var input = CraftingInput.of(3, 3, List.of(
            m, d, ItemStack.EMPTY,
            i, g, i,
            a, o, a
        ));
        assertTrue(recipe.matches(input, helper.getLevel()));
        var result = recipe.assemble(input, helper.getLevel().registryAccess());
        assertEquals("mover", BuiltInRegistries.ITEM.getKey(result.getItem()).getPath());

        helper.succeed();
    }

    public static void createPumpModule(GameTestHelper helper) {
        var name = "pump_module";
        var recipe = findRecipeNullable(helper, name);
        if (recipe == null) {
            if (PlatformAccess.getAccess().platformName().equalsIgnoreCase("fabric")) {
                helper.succeed();
                return;
            }
            helper.fail("Recipe not found");
            return;
        }

        var d = Items.GREEN_DYE.getDefaultInstance();
        var g = Items.GLASS.getDefaultInstance();
        var b = Items.LAVA_BUCKET.getDefaultInstance();
        var r = Items.REDSTONE.getDefaultInstance();
        var G = Items.GOLD_BLOCK.getDefaultInstance();
        var m = PlatformAccess.getAccess().registerObjects().markerBlock().get().blockItem.getDefaultInstance();
        var input = CraftingInput.of(3, 3, List.of(
            d, g, d,
            g, b, g,
            r, G, m
        ));

        assertTrue(recipe.matches(input, helper.getLevel()));
        var result = recipe.assemble(input, helper.getLevel().registryAccess());
        assertEquals(name, BuiltInRegistries.ITEM.getKey(result.getItem()).getPath());
        helper.succeed();
    }
}
