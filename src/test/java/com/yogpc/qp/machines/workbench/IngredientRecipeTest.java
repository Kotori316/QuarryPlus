package com.yogpc.qp.machines.workbench;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.kotori316.testutil.GameTestUtil;
import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.PowerTile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.gametest.GameTestDontPrefix;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@GameTestHolder(QuarryPlus.modID)
@GameTestDontPrefix
class IngredientRecipeTest {
    static final String BATCH = "IngredientRecipe";
    static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    static final IngredientRecipeSerialize SERIALIZE = new IngredientRecipeSerialize();

    static String itemNames(List<ItemStack> stacks) {
        if (stacks.isEmpty()) return "none";
        return stacks.stream().map(i -> "%d%s".formatted(i.getCount(), Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(i.getItem())).getPath()))
            .collect(Collectors.joining("_"));
    }

    @GameTest(template = GameTestUtil.EMPTY_STRUCTURE, batch = BATCH)
    void dummy() {
        assertTrue(advPumpRecipeParam().findAny().isPresent());
        assertTrue(notAdvPumpRecipeParam().findAny().isPresent());
    }

    @GameTest(template = GameTestUtil.EMPTY_STRUCTURE, batch = BATCH)
    void dummyBlock() throws IOException {
        JsonObject jsonObject;
        try (InputStream stream = getClass().getResourceAsStream("/data/quarryplus/recipes/dummy_block.json");
             Reader reader = new InputStreamReader(Objects.requireNonNull(stream))) {
            jsonObject = GSON.fromJson(reader, JsonObject.class);
        }
        var recipe = SERIALIZE.fromJson(jsonObject, ICondition.IContext.EMPTY);
        assertNotNull(recipe);
        assertEquals(5000 * PowerTile.ONE_FE, recipe.getRequiredEnergy());
        assertTrue(recipe.showInJEI());
        assertEquals(Items.EMERALD_BLOCK, recipe.output.getItem());
    }

    @GameTestGenerator
    List<TestFunction> advPumpRecipe() {
        return advPumpRecipeParam().map(
            i -> GameTestUtil.create(QuarryPlus.modID, BATCH, "adv_pump_recipe_%s".formatted(itemNames(i)), () -> advPumpRecipe(i))
        ).toList();
    }

    void advPumpRecipe(List<ItemStack> inventory) {
        var recipe = new IngredientRecipe(
            new ItemStack(Holder.BLOCK_ADV_PUMP), 3200000 * PowerTile.ONE_FE, true, List.of(
            new IngredientList(List.of(new IngredientWithCount(Ingredient.of(Holder.BLOCK_PUMP), 2), new IngredientWithCount(Ingredient.of(Holder.ITEM_PUMP_MODULE), 2))),
            new IngredientList(new IngredientWithCount(Ingredient.of(Holder.BLOCK_MINING_WELL), 2)),
            new IngredientList(new IngredientWithCount(Ingredient.of(Holder.BLOCK_MARKER), 3))
        ));

        assertEquals(Holder.BLOCK_ADV_PUMP.blockItem, recipe.output.getItem(), "Recipe is created correctly.");
        assertTrue(recipe.hasAllRequiredItems(inventory), "Has All Items: " + inventory);
    }

    static Stream<List<ItemStack>> advPumpRecipeParam() {
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
            Stream.of(Holder.ITEM_PUMP_MODULE, Holder.BLOCK_MINING_WELL, Holder.BLOCK_MARKER).map(i -> new ItemStack(i, 3)).toList(),
            Stream.<ItemLike>of(Holder.BLOCK_PUMP, Holder.BLOCK_MINING_WELL, Holder.BLOCK_MARKER).map(i -> new ItemStack(i, 10)).toList(),
            Stream.of(Holder.ITEM_PUMP_MODULE, Holder.BLOCK_MINING_WELL, Holder.BLOCK_MARKER).map(i -> new ItemStack(i, 10)).toList()
        );
    }

    @GameTestGenerator
    List<TestFunction> notAdvPumpRecipe() {
        return notAdvPumpRecipeParam().map(
            i -> GameTestUtil.create(QuarryPlus.modID, BATCH, "notAdvPumpRecipe_%s".formatted(itemNames(i)), () -> notAdvPumpRecipe(i))
        ).toList();
    }

    void notAdvPumpRecipe(List<ItemStack> inventory) {
        var recipe = new IngredientRecipe(
            new ItemStack(Holder.BLOCK_ADV_PUMP), 3200000 * PowerTile.ONE_FE, true, List.of(
            new IngredientList(List.of(new IngredientWithCount(Ingredient.of(Holder.BLOCK_PUMP), 2), new IngredientWithCount(Ingredient.of(Holder.ITEM_PUMP_MODULE), 2))),
            new IngredientList(new IngredientWithCount(Ingredient.of(Holder.BLOCK_MINING_WELL), 2)),
            new IngredientList(new IngredientWithCount(Ingredient.of(Holder.BLOCK_MARKER), 3))
        ));
        assertEquals(Holder.BLOCK_ADV_PUMP.blockItem, recipe.output.getItem(), "Recipe is created correctly.");
        assertFalse(recipe.hasAllRequiredItems(inventory), "Doesn't have All Items: " + inventory);
    }

    static Stream<List<ItemStack>> notAdvPumpRecipeParam() {
        return Stream.of(
            List.of(new ItemStack(Holder.BLOCK_PUMP, 1), new ItemStack(Holder.BLOCK_MINING_WELL, 2), new ItemStack(Holder.BLOCK_MARKER, 3)),
            List.of(new ItemStack(Holder.ITEM_PUMP_MODULE, 1), new ItemStack(Holder.BLOCK_MINING_WELL, 2), new ItemStack(Holder.BLOCK_MARKER, 3)),
            List.of(new ItemStack(Holder.ITEM_PUMP_MODULE, 2), new ItemStack(Holder.BLOCK_MINING_WELL, 1), new ItemStack(Holder.BLOCK_MARKER, 3)),
            List.of(new ItemStack(Holder.BLOCK_PUMP, 1), new ItemStack(Holder.ITEM_PUMP_MODULE, 1),
                new ItemStack(Holder.BLOCK_MINING_WELL, 2), new ItemStack(Holder.BLOCK_MARKER, 3)),
            Stream.<ItemLike>of(Holder.BLOCK_PUMP, Holder.BLOCK_MINING_WELL, Holder.BLOCK_MARKER).map(i -> new ItemStack(i, 2)).toList(),
            Stream.of(Holder.ITEM_PUMP_MODULE, Holder.BLOCK_MINING_WELL, Holder.BLOCK_MARKER).map(i -> new ItemStack(i, 2)).toList(),
            Stream.<ItemLike>of(Holder.BLOCK_PUMP, Holder.BLOCK_MINING_WELL, Holder.BLOCK_MARKER).map(i -> new ItemStack(i, 1)).toList(),
            Stream.of(Holder.ITEM_PUMP_MODULE, Holder.BLOCK_MINING_WELL, Holder.BLOCK_MARKER).map(i -> new ItemStack(i, 1)).toList(),
            List.of()
        );
    }
}
