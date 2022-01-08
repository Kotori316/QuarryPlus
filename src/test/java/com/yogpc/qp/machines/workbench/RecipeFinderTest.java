package com.yogpc.qp.machines.workbench;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.QuarryPlusTest;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecipeFinderTest extends QuarryPlusTest {
    @Test
    void recipeSize() {
        var recipe = new IngredientRecipe(new ResourceLocation(QuarryPlus.modID, "test_stone"), new ItemStack(Items.STONE, 64), 100L, false,
            List.of(new IngredientList(new IngredientWithCount(new ItemStack(Items.COBBLESTONE, 64)))));
        RecipeFinder finder = SimpleRecipeFinder.create(List.of(recipe));
        assertEquals(1, finder.recipeSize());
    }
}

record SimpleRecipeFinder(Map<ResourceLocation, WorkbenchRecipe> recipes) implements RecipeFinder {
    static SimpleRecipeFinder create(List<WorkbenchRecipe> recipes) {
        return new SimpleRecipeFinder(
            recipes.stream().collect(Collectors.toMap(WorkbenchRecipe::getId, Function.identity()))
        );
    }
}
