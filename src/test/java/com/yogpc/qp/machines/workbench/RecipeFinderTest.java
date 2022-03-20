package com.yogpc.qp.machines.workbench;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.QuarryPlusTest;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(QuarryPlusTest.class)
class RecipeFinderTest {
    static List<IngredientList> createList(ItemStack first, ItemStack... stacks) {
        return Stream.concat(Stream.of(first), Stream.of(stacks))
            .map(IngredientWithCount::new)
            .map(IngredientList::new)
            .toList();
    }

    static IngredientRecipe create(String name, long energy, ItemStack output, ItemStack inputFirst, ItemStack... inputs) {
        return new IngredientRecipe(new ResourceLocation(QuarryPlus.modID, "test_" + name), output, energy, false,
            createList(inputFirst, inputs));
    }

    @Test
    void recipeSize() {
        var recipe = create("stone", 400, new ItemStack(Items.STONE, 64), new ItemStack(Items.COBBLESTONE, 64));
        RecipeFinder finder = SimpleRecipeFinder.create(List.of(recipe));
        assertEquals(1, finder.recipeSize());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 16, 64})
    void findByOutput(int size) {
        var r1 = create("stone", 200L, new ItemStack(Items.STONE, 64), new ItemStack(Items.COBBLESTONE, 64));
        var r2 = create("dirt", 200L, new ItemStack(Items.STONE), new ItemStack(Items.DIRT, 4));
        var r3 = create("iron", 200L, new ItemStack(Items.STONE, 64), new ItemStack(Items.IRON_INGOT));
        var r4 = create("wood", 200L, new ItemStack(Items.OAK_LOG, 32), new ItemStack(Items.IRON_INGOT));
        RecipeFinder finder = SimpleRecipeFinder.create(List.of(r1, r2, r3, r4));

        {
            var result = finder.findRecipes(new ItemStack(Items.STONE, size));
            assertEquals(Set.of(r1, r2, r3), Set.copyOf(result), "Stack size of query should not affect.");
        }
        {
            var result = finder.findRecipes(new ItemStack(Items.OAK_LOG, size));
            assertEquals(Set.of(r4), Set.copyOf(result));
        }
    }

    @Nested
    class FindByInputTest {

        WorkbenchRecipe r1 = create("stone", 1000L, new ItemStack(Items.STONE, 64), new ItemStack(Items.COBBLESTONE, 64));
        WorkbenchRecipe r2 = create("dirt1", 1000L, new ItemStack(Items.STONE), new ItemStack(Items.DIRT, 4));
        WorkbenchRecipe r3 = create("iron1", 1000L, new ItemStack(Items.STONE, 64), new ItemStack(Items.IRON_INGOT));
        WorkbenchRecipe r4 = create("wood1", 1000L, new ItemStack(Items.OAK_LOG, 32), new ItemStack(Items.IRON_INGOT));
        WorkbenchRecipe r5 = create("dirt2", 1000L, new ItemStack(Items.COBBLESTONE, 48), new ItemStack(Items.DIRT, 24));
        WorkbenchRecipe r6 = create("iron2", 1000L, new ItemStack(Items.GOLD_INGOT, 1), new ItemStack(Items.IRON_INGOT, 16));
        RecipeFinder finder;

        @BeforeEach
        void setup() {
            finder = SimpleRecipeFinder.create(List.of(r1, r2, r3, r4, r5, r6));
        }

        @Test
        void empty() {
            var result = finder.getRecipes(List.of(ItemStack.EMPTY));
            assertTrue(result.isEmpty());
        }

        @Test
        void none() {
            var result = finder.getRecipes(List.of());
            assertTrue(result.isEmpty());
        }

        @Test
        void dirt1() {
            var result = finder.getRecipes(List.of(new ItemStack(Items.DIRT, 1)));
            assertTrue(result.isEmpty());
        }

        @ParameterizedTest
        @ValueSource(ints = {4, 10, 20, 23})
        void dirt4To23(int size) {
            var result = finder.getRecipes(List.of(new ItemStack(Items.DIRT, size)));
            assertEquals(List.of(r2), result);
        }

        @ParameterizedTest
        @ValueSource(ints = {24, 32, 64})
        void dirt24To64(int size) {
            var result = finder.getRecipes(List.of(new ItemStack(Items.DIRT, size)));
            assertEquals(Set.of(r2, r5), Set.copyOf(result));
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 4, 10, 15})
        void iron1(int size) {
            var result = finder.getRecipes(List.of(new ItemStack(Items.IRON_INGOT, size)));
            assertEquals(Set.of(r3, r4), Set.copyOf(result));
        }

        @ParameterizedTest
        @ValueSource(ints = {16, 24, 32, 64})
        void iron16(int size) {
            var result = finder.getRecipes(List.of(new ItemStack(Items.IRON_INGOT, size)));
            assertEquals(Set.of(r3, r4, r6), Set.copyOf(result));
        }

        @Test
        void all() {
            var result = finder.getRecipes(List.of(
                new ItemStack(Items.IRON_INGOT, 64),
                new ItemStack(Items.DIRT, 64),
                new ItemStack(Items.COBBLESTONE, 64)
            ));
            assertEquals(Set.of(r1, r2, r3, r4, r5, r6), Set.copyOf(result));
        }
    }
}

record SimpleRecipeFinder(Map<ResourceLocation, WorkbenchRecipe> recipes) implements RecipeFinder {
    static SimpleRecipeFinder create(List<WorkbenchRecipe> recipes) {
        return new SimpleRecipeFinder(
            recipes.stream().collect(Collectors.toMap(WorkbenchRecipe::getId, Function.identity()))
        );
    }
}
