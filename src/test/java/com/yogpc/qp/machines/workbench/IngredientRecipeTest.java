package com.yogpc.qp.machines.workbench;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlusTest;
import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.machines.advpump.BlockAdvPump;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.yogpc.qp.QuarryPlusTest.id;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(QuarryPlusTest.class)
class IngredientRecipeTest {
    static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    static final IngredientRecipeSerialize SERIALIZE = new IngredientRecipeSerialize();

    @Test
    void dummy() {
        assertTrue(advPumpRecipe().findAny().isPresent());
        assertTrue(notAdvPumpRecipe().findAny().isPresent());
    }

    @Test
    void dummyBlock() throws IOException {
        JsonObject jsonObject;
        try (InputStream stream = getClass().getResourceAsStream("/data/quarryplus/recipes/dummy_block.json");
             Reader reader = new InputStreamReader(Objects.requireNonNull(stream))) {
            jsonObject = GSON.fromJson(reader, JsonObject.class);
        }
        var recipe = SERIALIZE.fromJson(id("dummy_block_recipe"), jsonObject, ICondition.IContext.EMPTY);
        assertNotNull(recipe);
        assertEquals(5000 * PowerTile.ONE_FE, recipe.getRequiredEnergy());
        assertTrue(recipe.showInJEI());
        assertEquals(Items.EMERALD_BLOCK, recipe.getResultItem().getItem());
    }

    @ParameterizedTest
    @MethodSource
    void advPumpRecipe(List<ItemStack> inventory) {
        var recipe = new IngredientRecipe(
            id(BlockAdvPump.NAME), new ItemStack(Holder.BLOCK_ADV_PUMP), 3200000 * PowerTile.ONE_FE, true, List.of(
            new IngredientList(List.of(new IngredientWithCount(Ingredient.of(Holder.BLOCK_PUMP), 2), new IngredientWithCount(Ingredient.of(Holder.ITEM_PUMP_MODULE), 2))),
            new IngredientList(new IngredientWithCount(Ingredient.of(Holder.BLOCK_MINING_WELL), 2)),
            new IngredientList(new IngredientWithCount(Ingredient.of(Holder.BLOCK_MARKER), 3))
        ));

        assertEquals(Holder.BLOCK_ADV_PUMP.blockItem, recipe.getResultItem().getItem(), "Recipe is created correctly.");
        assertTrue(recipe.hasAllRequiredItems(inventory), "Has All Items: " + inventory);
    }

    static Stream<List<ItemStack>> advPumpRecipe() {
        return Stream.of(
            List.of(new ItemStack(Holder.BLOCK_PUMP, 2), new ItemStack(Holder.BLOCK_MINING_WELL, 2), new ItemStack(Holder.BLOCK_MARKER, 3)),
            List.of(new ItemStack(Holder.ITEM_PUMP_MODULE, 2), new ItemStack(Holder.BLOCK_MINING_WELL, 2), new ItemStack(Holder.BLOCK_MARKER, 3)),
            List.of(new ItemStack(Holder.BLOCK_PUMP, 2), new ItemStack(Holder.ITEM_PUMP_MODULE, 2),
                new ItemStack(Holder.BLOCK_MINING_WELL, 2), new ItemStack(Holder.BLOCK_MARKER, 3)),
            List.of(new ItemStack(Holder.BLOCK_PUMP, 1), new ItemStack(Holder.ITEM_PUMP_MODULE, 2),
                new ItemStack(Holder.BLOCK_MINING_WELL, 2), new ItemStack(Holder.BLOCK_MARKER, 3)),
            List.of(new ItemStack(Holder.BLOCK_PUMP, 2), new ItemStack(Holder.ITEM_PUMP_MODULE, 1),
                new ItemStack(Holder.BLOCK_MINING_WELL, 2), new ItemStack(Holder.BLOCK_MARKER, 3)),
            Stream.<ItemLike>of(Holder.BLOCK_PUMP, Holder.BLOCK_MINING_WELL, Holder.BLOCK_MARKER).map(i -> new ItemStack(i, 3)).toList(),
            Stream.<ItemLike>of(Holder.ITEM_PUMP_MODULE, Holder.BLOCK_MINING_WELL, Holder.BLOCK_MARKER).map(i -> new ItemStack(i, 3)).toList(),
            Stream.<ItemLike>of(Holder.BLOCK_PUMP, Holder.BLOCK_MINING_WELL, Holder.BLOCK_MARKER).map(i -> new ItemStack(i, 10)).toList(),
            Stream.<ItemLike>of(Holder.ITEM_PUMP_MODULE, Holder.BLOCK_MINING_WELL, Holder.BLOCK_MARKER).map(i -> new ItemStack(i, 10)).toList()
        );
    }

    @ParameterizedTest
    @MethodSource
    void notAdvPumpRecipe(List<ItemStack> inventory) {
        var recipe = new IngredientRecipe(
            id(BlockAdvPump.NAME), new ItemStack(Holder.BLOCK_ADV_PUMP), 3200000 * PowerTile.ONE_FE, true, List.of(
            new IngredientList(List.of(new IngredientWithCount(Ingredient.of(Holder.BLOCK_PUMP), 2), new IngredientWithCount(Ingredient.of(Holder.ITEM_PUMP_MODULE), 2))),
            new IngredientList(new IngredientWithCount(Ingredient.of(Holder.BLOCK_MINING_WELL), 2)),
            new IngredientList(new IngredientWithCount(Ingredient.of(Holder.BLOCK_MARKER), 3))
        ));
        assertEquals(Holder.BLOCK_ADV_PUMP.blockItem, recipe.getResultItem().getItem(), "Recipe is created correctly.");
        assertFalse(recipe.hasAllRequiredItems(inventory), "Doesn't have All Items: " + inventory);
    }

    static Stream<List<ItemStack>> notAdvPumpRecipe() {
        return Stream.of(
            List.of(new ItemStack(Holder.BLOCK_PUMP, 1), new ItemStack(Holder.BLOCK_MINING_WELL, 2), new ItemStack(Holder.BLOCK_MARKER, 3)),
            List.of(new ItemStack(Holder.ITEM_PUMP_MODULE, 1), new ItemStack(Holder.BLOCK_MINING_WELL, 2), new ItemStack(Holder.BLOCK_MARKER, 3)),
            List.of(new ItemStack(Holder.ITEM_PUMP_MODULE, 2), new ItemStack(Holder.BLOCK_MINING_WELL, 1), new ItemStack(Holder.BLOCK_MARKER, 3)),
            List.of(new ItemStack(Holder.BLOCK_PUMP, 1), new ItemStack(Holder.ITEM_PUMP_MODULE, 1),
                new ItemStack(Holder.BLOCK_MINING_WELL, 2), new ItemStack(Holder.BLOCK_MARKER, 3)),
            Stream.<ItemLike>of(Holder.BLOCK_PUMP, Holder.BLOCK_MINING_WELL, Holder.BLOCK_MARKER).map(i -> new ItemStack(i, 2)).toList(),
            Stream.<ItemLike>of(Holder.ITEM_PUMP_MODULE, Holder.BLOCK_MINING_WELL, Holder.BLOCK_MARKER).map(i -> new ItemStack(i, 2)).toList(),
            Stream.<ItemLike>of(Holder.BLOCK_PUMP, Holder.BLOCK_MINING_WELL, Holder.BLOCK_MARKER).map(i -> new ItemStack(i, 1)).toList(),
            Stream.<ItemLike>of(Holder.ITEM_PUMP_MODULE, Holder.BLOCK_MINING_WELL, Holder.BLOCK_MARKER).map(i -> new ItemStack(i, 1)).toList(),
            List.of()
        );
    }
}
