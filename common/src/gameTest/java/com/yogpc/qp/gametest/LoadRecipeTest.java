package com.yogpc.qp.gametest;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryDataComponents;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.advquarry.AdvQuarryBlock;
import com.yogpc.qp.machine.marker.ChunkMarkerBlock;
import com.yogpc.qp.machine.marker.FlexibleMarkerBlock;
import com.yogpc.qp.machine.module.FilterModuleItem;
import com.yogpc.qp.machine.module.RepeatTickModuleItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
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

    public static void createFlexibleMarker(GameTestHelper helper) {
        var recipe = findRecipe(helper, FlexibleMarkerBlock.NAME);

        var d = Items.GREEN_DYE.getDefaultInstance();
        var m = PlatformAccess.getAccess().registerObjects().markerBlock().get().blockItem.getDefaultInstance();
        var input = CraftingInput.of(3, 2, List.of(
            d, d, d,
            m, m, m
        ));
        assertTrue(recipe.matches(input, helper.getLevel()));
        var result = recipe.assemble(input, helper.getLevel().registryAccess());
        assertEquals(
            PlatformAccess.getAccess().registerObjects().flexibleMarkerBlock().get().blockItem,
            result.getItem()
        );

        helper.succeed();
    }

    public static void createFlexibleMarker2(GameTestHelper helper) {
        var recipe = findRecipe(helper, FlexibleMarkerBlock.NAME + "_from_" + ChunkMarkerBlock.NAME);

        var e = ItemStack.EMPTY;
        var d = Items.GREEN_DYE.getDefaultInstance();
        var m = PlatformAccess.getAccess().registerObjects().chunkMarkerBlock().get().blockItem.getDefaultInstance();
        var input = CraftingInput.of(3, 2, List.of(
            d, d, d,
            e, m, e
        ));
        assertTrue(recipe.matches(input, helper.getLevel()));
        var result = recipe.assemble(input, helper.getLevel().registryAccess());
        assertEquals(
            PlatformAccess.getAccess().registerObjects().flexibleMarkerBlock().get().blockItem,
            result.getItem()
        );

        helper.succeed();
    }

    public static void createChunkMarker(GameTestHelper helper) {
        var recipe = findRecipe(helper, ChunkMarkerBlock.NAME);

        var r = Items.REDSTONE.getDefaultInstance();
        var m = PlatformAccess.getAccess().registerObjects().markerBlock().get().blockItem.getDefaultInstance();
        var input = CraftingInput.of(3, 2, List.of(
            r, r, r,
            m, m, m
        ));
        assertTrue(recipe.matches(input, helper.getLevel()));
        var result = recipe.assemble(input, helper.getLevel().registryAccess());
        assertEquals(
            PlatformAccess.getAccess().registerObjects().chunkMarkerBlock().get().blockItem,
            result.getItem()
        );

        helper.succeed();
    }

    public static void createChunkMarker2(GameTestHelper helper) {
        var recipe = findRecipe(helper, ChunkMarkerBlock.NAME + "_from_" + FlexibleMarkerBlock.NAME);

        var e = ItemStack.EMPTY;
        var r = Items.REDSTONE.getDefaultInstance();
        var m = PlatformAccess.getAccess().registerObjects().flexibleMarkerBlock().get().blockItem.getDefaultInstance();
        var input = CraftingInput.of(3, 2, List.of(
            r, r, r,
            e, m, e
        ));
        assertTrue(recipe.matches(input, helper.getLevel()));
        var result = recipe.assemble(input, helper.getLevel().registryAccess());
        assertEquals(
            PlatformAccess.getAccess().registerObjects().chunkMarkerBlock().get().blockItem,
            result.getItem()
        );

        helper.succeed();
    }

    public static void createQuarry(GameTestHelper helper) {
        var recipe = findRecipe(helper, "quarry");

        var D = Items.DROPPER.getDefaultInstance();
        var R = Items.REDSTONE_BLOCK.getDefaultInstance();
        ItemStack g;
        if (PlatformAccess.getAccess().platformName().equalsIgnoreCase("fabric")) {
            g = Items.GOLDEN_PICKAXE.getDefaultInstance();
        } else {
            g = Items.DIAMOND_PICKAXE.getDefaultInstance();
        }
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
        if (PlatformAccess.getAccess().platformName().equalsIgnoreCase("fabric")) {
            assertNull(recipe, "This recipe(%s) must not be loaded in fabric".formatted(name));
            helper.succeed();
            return;
        }
        assertNotNull(recipe, "Recipe not found");

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

    public static void createBedrockModule(GameTestHelper helper) {
        var name = "remove_bedrock_module";
        var recipe = findRecipe(helper, name);

        ItemStack d;
        if (PlatformAccess.getAccess().platformName().equalsIgnoreCase("fabric")) {
            d = ItemStack.EMPTY;
        } else {
            d = Items.DIAMOND_BLOCK.getDefaultInstance();
        }
        var o = Items.OBSIDIAN.getDefaultInstance();
        var m = PlatformAccess.getAccess().registerObjects().markerBlock().get().blockItem.getDefaultInstance();
        var input = CraftingInput.of(3, 3, List.of(
            o, o, o,
            d, m, d,
            d, m, d
        ));

        assertTrue(recipe.matches(input, helper.getLevel()));
        var result = recipe.assemble(input, helper.getLevel().registryAccess());
        assertEquals(name, BuiltInRegistries.ITEM.getKey(result.getItem()).getPath());
        helper.succeed();
    }

    public static void createExpModule(GameTestHelper helper) {
        var name = "exp_module";
        var recipe = findRecipeNullable(helper, name);
        if (PlatformAccess.getAccess().platformName().equalsIgnoreCase("fabric")) {
            assertNull(recipe, "This recipe(%s) must not be loaded in fabric".formatted(name));
            helper.succeed();
            return;
        }
        assertNotNull(recipe, "Recipe not found");

        var h = Items.HAY_BLOCK.getDefaultInstance();
        var e = Items.ENDER_PEARL.getDefaultInstance();
        var G = Items.GOLD_BLOCK.getDefaultInstance();
        var p = Items.POTION.getDefaultInstance();
        var m = PlatformAccess.getAccess().registerObjects().markerBlock().get().blockItem.getDefaultInstance();
        var input = CraftingInput.of(3, 3, List.of(
            e, p, e,
            m, h, p,
            G, h, G
        ));
        assertTrue(recipe.matches(input, helper.getLevel()));
        var result = recipe.assemble(input, helper.getLevel().registryAccess());
        assertEquals(name, BuiltInRegistries.ITEM.getKey(result.getItem()).getPath());
        helper.succeed();
    }

    public static void installBedrockModuleQuarry(GameTestHelper helper) {
        if (true) {
            helper.succeed();
            return;
        }
        var name = "install_bedrock_module_quarry";
        var recipe = findRecipeNullable(helper, name);
        if (!PlatformAccess.getAccess().platformName().equalsIgnoreCase("fabric")) {
            assertNull(recipe, "This recipe(%s) must be loaded only in fabric".formatted(name));
            helper.succeed();
            return;
        }
        assertNotNull(recipe, "Recipe not found");
        var q = PlatformAccess.getAccess().registerObjects().quarryBlock().get().blockItem.getDefaultInstance();
        var m = PlatformAccess.getAccess().registerObjects().bedrockModuleItem().get().getDefaultInstance();
        var input = CraftingInput.of(2, 1, List.of(
            q, m
        ));
        assertTrue(recipe.matches(input, helper.getLevel()));
        var result = recipe.assemble(input, helper.getLevel().registryAccess());
        var condition = result.get(QuarryDataComponents.QUARRY_REMOVE_BEDROCK_COMPONENT);
        assertNotNull(condition);
        assertTrue(condition);
        helper.succeed();
    }

    public static void installBedrockModuleQuarry2(GameTestHelper helper) {
        if (true) {
            helper.succeed();
            return;
        }
        var name = "install_bedrock_module_quarry";
        var recipe = findRecipeNullable(helper, name);
        if (!PlatformAccess.getAccess().platformName().equalsIgnoreCase("fabric")) {
            assertNull(recipe, "This recipe(%s) must be loaded only in fabric".formatted(name));
            helper.succeed();
            return;
        }
        assertNotNull(recipe, "Recipe not found");
        var q = PlatformAccess.getAccess().registerObjects().quarryBlock().get().blockItem.getDefaultInstance();
        var m = PlatformAccess.getAccess().registerObjects().bedrockModuleItem().get().getDefaultInstance();
        q.set(QuarryDataComponents.QUARRY_REMOVE_BEDROCK_COMPONENT, true);
        var input = CraftingInput.of(2, 1, List.of(
            q, m
        ));
        assertFalse(recipe.matches(input, helper.getLevel()), "Not to install module twice");
        helper.succeed();
    }

    public static void installBedrockModuleQuarry3(GameTestHelper helper) {
        var name = "install_bedrock_module_quarry";
        var recipe = findRecipeNullable(helper, name);
        assertNull(recipe, "This recipe must not be loaded in all platforms");
        helper.succeed();
    }

    public static void createRepeatTickModule(GameTestHelper helper) {
        var name = RepeatTickModuleItem.NAME;
        var recipe = findRecipeNullable(helper, name);
        if (PlatformAccess.getAccess().platformName().equalsIgnoreCase("fabric")) {
            assertNull(recipe, "This recipe(%s) must not be loaded in fabric".formatted(name));
            helper.succeed();
            return;
        }
        assertNotNull(recipe, "Recipe not found");

        var a = Items.AMETHYST_SHARD.getDefaultInstance();
        var p = Items.PRISMARINE_SHARD.getDefaultInstance();
        var w = PotionContents.createItemStack(Items.LINGERING_POTION, Potions.STRONG_SWIFTNESS);
        var m = PlatformAccess.getAccess().registerObjects().markerBlock().get().blockItem.getDefaultInstance();
        var input = CraftingInput.of(3, 3, List.of(
            a, p, a,
            p, w, p,
            a, p, m
        ));

        assertTrue(recipe.matches(input, helper.getLevel()));
        var result = recipe.assemble(input, helper.getLevel().registryAccess());
        assertEquals(name, BuiltInRegistries.ITEM.getKey(result.getItem()).getPath());
        helper.succeed();
    }

    public static void createFilterModule(GameTestHelper helper) {
        var name = FilterModuleItem.NAME;
        var recipe = findRecipeNullable(helper, name);
        if (PlatformAccess.getAccess().platformName().equalsIgnoreCase("fabric")) {
            assertNull(recipe, "This recipe(%s) must not be loaded in fabric".formatted(name));
            helper.succeed();
            return;
        }
        assertNotNull(recipe, "Recipe not found");

        var b = Items.BOOK.getDefaultInstance();
        var e = Items.ENCHANTED_BOOK.getDefaultInstance();
        var p = Items.ENDER_PEARL.getDefaultInstance();
        var m = PlatformAccess.getAccess().registerObjects().markerBlock().get().blockItem.getDefaultInstance();
        var input = CraftingInput.of(2, 2, List.of(
            b, e,
            p, m
        ));

        assertTrue(recipe.matches(input, helper.getLevel()));
        var result = recipe.assemble(input, helper.getLevel().registryAccess());
        assertEquals(name, BuiltInRegistries.ITEM.getKey(result.getItem()).getPath());
        helper.succeed();
    }

    public static void createAdvQuarry(GameTestHelper helper) {
        var recipe = findRecipe(helper, AdvQuarryBlock.NAME);

        var d = Items.DIAMOND_BLOCK.getDefaultInstance();
        var e = Items.EMERALD_BLOCK.getDefaultInstance();
        var q = new ItemStack(PlatformAccess.getAccess().registerObjects().quarryBlock().get());
        var i = Items.ENDER_EYE.getDefaultInstance();
        var h = Items.DRAGON_HEAD.getDefaultInstance();
        var s = Items.NETHER_STAR.getDefaultInstance();

        var input = CraftingInput.of(3, 3, List.of(
            d, h, d,
            q, s, q,
            e, i, e
        ));
        assertTrue(recipe.matches(input, helper.getLevel()));
        var result = recipe.assemble(input, helper.getLevel().registryAccess());
        assertEquals(
            PlatformAccess.getAccess().registerObjects().advQuarryBlock().get().blockItem,
            result.getItem()
        );

        helper.succeed();
    }
}
