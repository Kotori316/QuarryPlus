package com.yogpc.qp.machines.workbench;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.yogpc.qp.QuarryPlusTest;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IngredientListTest extends QuarryPlusTest {
    static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    @Test
    void singleMatch1() {
        var list = new IngredientList(new IngredientWithCount(new ItemStack(Items.APPLE)));
        assertAll(
            () -> assertTrue(list.test(new ItemStack(Items.APPLE)), "Apple"),
            () -> assertFalse(list.test(ItemStack.EMPTY), "Empty"),
            () -> assertFalse(list.test(new ItemStack(Items.STONE)), "Stone")
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 5, 8, 10, 64})
    void singleShrink1(int count) {
        var list = new IngredientList(new IngredientWithCount(new ItemStack(Items.APPLE)));
        var stack = new ItemStack(Items.APPLE, count);
        assertEquals(count, stack.getCount());
        var result = list.shrink(stack);
        assertEquals(count - 1, stack.getCount());
        assertTrue(result);
    }

    @Test
    void singleShrink2() {
        var list = new IngredientList(new IngredientWithCount(new ItemStack(Items.APPLE)));
        var stack = new ItemStack(Items.APPLE, 1);
        assertEquals(1, stack.getCount());
        var result = list.shrink(stack);
        assertEquals(0, stack.getCount());
        assertTrue(stack.isEmpty());
        assertTrue(result);
    }

    @Test
    void singleShrink3() {
        var list = new IngredientList(new IngredientWithCount(new ItemStack(Items.APPLE)));
        var stack = new ItemStack(Items.STONE, 1);
        assertEquals(1, stack.getCount());
        var result = list.shrink(stack);
        assertEquals(1, stack.getCount());
        assertFalse(result);
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 8, 10, 64})
    void singleShrink4(int count) {
        var list = new IngredientList(new IngredientWithCount(new ItemStack(Items.APPLE, 3)));
        var stack = new ItemStack(Items.APPLE, count);
        assertEquals(count, stack.getCount());
        var result = list.shrink(stack);
        assertEquals(count - 3, stack.getCount());
        assertTrue(result);
    }

    @Test
    void twoMatch1() {
        var list = new IngredientList(List.of(new IngredientWithCount(new ItemStack(Items.APPLE, 10)),
            new IngredientWithCount(new ItemStack(Items.GOLDEN_APPLE, 5))));
        assertAll(
            () -> assertTrue(list.test(new ItemStack(Items.APPLE, 10))),
            () -> assertTrue(list.test(new ItemStack(Items.APPLE, 20))),
            () -> assertTrue(list.test(new ItemStack(Items.GOLDEN_APPLE, 20))),
            () -> assertTrue(list.test(new ItemStack(Items.GOLDEN_APPLE, 5))),
            () -> assertFalse(list.test(new ItemStack(Items.GOLDEN_APPLE, 4))),
            () -> assertFalse(list.test(ItemStack.EMPTY), "Empty"),
            () -> assertFalse(list.test(new ItemStack(Items.STONE)), "Stone")
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {10, 15, 64, 128})
    void twoShrink1(int count) {
        var list = new IngredientList(List.of(new IngredientWithCount(new ItemStack(Items.APPLE, 10)),
            new IngredientWithCount(new ItemStack(Items.GOLDEN_APPLE, 5))));
        var apple = new ItemStack(Items.APPLE, count);
        var golden = new ItemStack(Items.GOLDEN_APPLE, count);
        assertTrue(list.shrink(apple));
        assertTrue(list.shrink(golden));
        assertEquals(count - 10, apple.getCount());
        assertEquals(count - 5, golden.getCount());
    }

    @Test
    void singleToJson1() {
        var list = new IngredientList(new IngredientWithCount(new ItemStack(Items.APPLE)));
        var json = list.toJson();
        assertTrue(json.isJsonObject(), "Expected JsonObject: " + json.getClass());
        // language=json
        var expected = GSON.fromJson("""
            {
              "item": "minecraft:apple",
              "count": 1
            }
            """, JsonObject.class);
        assertEquals(expected, json);
    }

    @Test
    void singleFromJson1() {
        // language=json
        var json = GSON.fromJson("""
            {
              "item": "minecraft:apple",
              "count": 5
            }
            """, JsonObject.class);
        var list = IngredientList.fromJson(json);
        assertEquals(json, list.toJson());
        assertTrue(list.test(new ItemStack(Items.APPLE, 5)));
        assertFalse(list.test(new ItemStack(Items.APPLE, 1)));
    }

    @Test
    void twoToJson1() {
        var list = new IngredientList(List.of(new IngredientWithCount(new ItemStack(Items.APPLE, 10)),
            new IngredientWithCount(new ItemStack(Items.GOLDEN_APPLE, 5))));
        var json = list.toJson();
        assertTrue(json.isJsonArray(), "Expected Array: " + json.getClass());
        assertEquals(2, json.getAsJsonArray().size());
        // language=json
        var expected = GSON.fromJson("""
            [
                {"item": "minecraft:apple", "count": 10},
                {"item": "minecraft:golden_apple", "count": 5}
            ]
            """, JsonArray.class);
        assertEquals(expected, json);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10, 64})
    void toNetwork1(int count) {
        var i = new IngredientList(new IngredientWithCount(new ItemStack(Items.APPLE, count)));
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        i.toPacket(buffer);
        var fromNetwork = IngredientList.fromPacket(buffer);
        assertEquals(i.toJson(), fromNetwork.toJson());
    }

    @Test
    void toNetwork2() {
        var list = new IngredientList(List.of(new IngredientWithCount(new ItemStack(Items.APPLE, 10)),
            new IngredientWithCount(new ItemStack(Items.GOLDEN_APPLE, 5))));
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        list.toPacket(buffer);
        var fromNetwork = IngredientList.fromPacket(buffer);
        assertEquals(list.toJson(), fromNetwork.toJson());
    }

    @Test
    void invalid1() {
        var l = new IngredientList(new IngredientWithCount(Ingredient.EMPTY, 1));
        assertTrue(l.invalid());
    }

    @Test
    void invalid2() {
        var l = new IngredientList(new IngredientWithCount(new ItemStack(Items.APPLE)));
        assertFalse(l.invalid());
    }

    @Test
    void invalid3() {
        var l = new IngredientList(List.of(new IngredientWithCount(Ingredient.EMPTY, 1), new IngredientWithCount(new ItemStack(Items.APPLE))));
        assertFalse(l.invalid());
    }
}