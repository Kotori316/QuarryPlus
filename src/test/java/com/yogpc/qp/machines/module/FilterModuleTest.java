package com.yogpc.qp.machines.module;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.TestCraftingContainer;
import com.yogpc.qp.machines.ItemKey;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class FilterModuleTest {
    @Test
    void emptyInstance() {
        var module = new FilterModule(List.of());
        assertTrue(module.getItemKeys().isEmpty());
    }

    @Test
    void emptyInstance2() {
        var module = new FilterModule((ListTag) null);
        assertTrue(module.getItemKeys().isEmpty());
    }

    @Test
    void instance() {
        var module = new FilterModule(List.of(new ItemKey(Items.BEDROCK, null)));
        assertEquals(List.of(new ItemKey(Items.BEDROCK, null)), module.getItemKeys());
    }

    @Test
    void instance20() {
        var itemKeys = List.of(
            new ItemKey(Items.STONE, null),
            new ItemKey(Items.GRANITE, null),
            new ItemKey(Items.POLISHED_GRANITE, null),
            new ItemKey(Items.DIORITE, null),
            new ItemKey(Items.POLISHED_DIORITE, null),
            new ItemKey(Items.ANDESITE, null),
            new ItemKey(Items.POLISHED_ANDESITE, null),
            new ItemKey(Items.DEEPSLATE, null),
            new ItemKey(Items.COBBLED_DEEPSLATE, null),
            new ItemKey(Items.POLISHED_DEEPSLATE, null),
            new ItemKey(Items.CALCITE, null),
            new ItemKey(Items.TUFF, null),
            new ItemKey(Items.DRIPSTONE_BLOCK, null),
            new ItemKey(Items.GRASS_BLOCK, null),
            new ItemKey(Items.DIRT, null),
            new ItemKey(Items.COARSE_DIRT, null),
            new ItemKey(Items.PODZOL, null),
            new ItemKey(Items.ROOTED_DIRT, null),
            new ItemKey(Items.MUD, null),
            new ItemKey(Items.CRIMSON_NYLIUM, null)
        );
        assertEquals(20, itemKeys.size());
        var module = new FilterModule(itemKeys);
        assertEquals(itemKeys, module.getItemKeys());
    }

    @Nested
    class ConverterTest {
        @Test
        void empty() {
            var module = new FilterModule(List.of());
            var converter = module.createConverter();
            assertAll(items1().map(ItemStack::new).map(converter::map).map(i ->
                () -> assertFalse(i.isEmpty())
            ));
        }

        @Test
        void cobblestone() {
            var module = new FilterModule(List.of(new ItemKey(Items.COBBLESTONE, null)));
            var converter = module.createConverter();
            assertAll(items1().map(ItemStack::new).map(converter::map).map(i ->
                () -> assertFalse(i.isEmpty())
            ));
            assertTrue(converter.map(new ItemStack(Items.COBBLESTONE)).isEmpty());
        }

        @Test
        void cobblestoneAndBedrock() {
            var module = new FilterModule(List.of(new ItemKey(Items.COBBLESTONE, null), new ItemKey(Items.BEDROCK, null)));
            var converter = module.createConverter();
            assertAll(items1().map(ItemStack::new).map(converter::map).map(i ->
                () -> assertFalse(i.isEmpty())
            ));
            assertTrue(converter.map(new ItemStack(Items.COBBLESTONE)).isEmpty());
            assertTrue(converter.map(new ItemStack(Items.BEDROCK)).isEmpty());
        }
    }

    @Nested
    class SerializeTest {
        @Test
        void getFromNullTag() {
            assertTrue(FilterModule.getFromTag(null).isEmpty());
        }

        @Test
        void getFromEmptyTag() {
            assertTrue(FilterModule.getFromTag(new ListTag()).isEmpty());
        }

        @Test
        void getFromEmptyItems() {
            assertTrue(FilterModule.getFromItems(List.of()).isEmpty());
        }

        @Test
        void cycle() {
            var keys = items1().map(i -> new ItemKey(i, null)).toList();
            var listTag = FilterModule.getFromItemKeys(keys.stream());
            var fromTag = FilterModule.getFromTag(listTag);
            assertEquals(Set.copyOf(keys), Set.copyOf(fromTag));
        }
    }

    @Nested
    class RecipeTest {
        @Test
        void expandRecipeMatch() {
            var recipe = new FilterModuleExpandRecipe(ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, "test_expand_recipe"), CraftingBookCategory.MISC, Items.STONE);
            assertTrue(recipe.matches(new TestCraftingContainer(List.of(
                "ccc",
                "cmc",
                "ccc"
            ), Map.of('c', Items.CHEST.getDefaultInstance(), 'm', Items.STONE.getDefaultInstance())), null));
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 2, 3, 4})
        void expandRecipeMatch2(int rows) {
            var recipe = new FilterModuleExpandRecipe(ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, "test_expand_recipe"), CraftingBookCategory.MISC, Items.STONE);
            var stone = Items.STONE.getDefaultInstance();
            stone.getOrCreateTag().putInt(FilterModuleItem.KEY_ITEM_ROWS, rows);
            assertTrue(recipe.matches(new TestCraftingContainer(List.of(
                "ccc",
                "cmc",
                "ccc"
            ), Map.of('c', Items.CHEST.getDefaultInstance(), 'm', stone)), null));
        }

        @Test
        void expandRecipeNoMatch1() {
            var recipe = new FilterModuleExpandRecipe(ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, "test_expand_recipe"), CraftingBookCategory.MISC, Items.STONE);
            assertFalse(recipe.matches(new TestCraftingContainer(List.of(
                "ccc",
                "cmc",
                "ccc"
            ), Map.of('c', Items.CHEST.getDefaultInstance(), 'm', Items.COBBLESTONE.getDefaultInstance())), null));
        }

        @ParameterizedTest
        @ValueSource(ints = {5, 6})
        void expandRecipeNoMatch2(int rows) {
            var recipe = new FilterModuleExpandRecipe(ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, "test_expand_recipe"), CraftingBookCategory.MISC, Items.STONE);
            var stone = Items.STONE.getDefaultInstance();
            stone.getOrCreateTag().putInt(FilterModuleItem.KEY_ITEM_ROWS, rows);
            assertFalse(recipe.matches(new TestCraftingContainer(List.of(
                "ccc",
                "cmc",
                "ccc"
            ), Map.of('c', Items.CHEST.getDefaultInstance(), 'm', stone)), null));
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 2, 3, 4})
        void assemble(int rows) {
            var recipe = new FilterModuleExpandRecipe(ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, "test_expand_recipe"), CraftingBookCategory.MISC, Items.STONE);
            var stone = Items.STONE.getDefaultInstance();
            stone.getOrCreateTag().putInt(FilterModuleItem.KEY_ITEM_ROWS, rows);
            var container = new TestCraftingContainer(List.of(
                "ccc",
                "cmc",
                "ccc"
            ), Map.of('c', Items.CHEST.getDefaultInstance(), 'm', stone));
            var result = recipe.assemble(container, null);
            assertTrue(result.is(Items.STONE));
            var row = FilterModuleItem.getRowsFromStack(result);
            assertEquals(rows + 2, row);
        }
    }

    static Stream<Item> items1() {
        return Stream.of(
            Items.STONE,
            Items.ANDESITE,
            Items.POLISHED_GRANITE);
    }
}
