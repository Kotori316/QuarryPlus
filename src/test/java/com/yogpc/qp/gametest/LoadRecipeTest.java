package com.yogpc.qp.gametest;

import com.google.gson.JsonObject;
import com.kotori316.testutil.GameTestUtil;
import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.machines.workbench.WorkbenchRecipe;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.gametest.GameTestHolder;
import org.junit.platform.commons.function.Try;
import org.junit.platform.commons.support.ReflectionSupport;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@GameTestHolder(QuarryPlus.modID)
public final class LoadRecipeTest {
    @GameTest(template = GameTestUtil.EMPTY_STRUCTURE)
    public void accessTag(GameTestHelper helper) {
        assertAll(
            () -> assertTrue(new ItemStack(Items.STONE).is(Tags.Items.STONE), "Tag STONE"),
            () -> assertTrue(new ItemStack(Items.DIRT).is(ItemTags.DIRT), "Tag DIRT"),
            () -> assertTrue(new ItemStack(Items.ANDESITE).is(Tags.Items.STONE), "Tag STONE"),
            () -> assertTrue(new ItemStack(Items.IRON_INGOT).is(ItemTags.BEACON_PAYMENT_ITEMS), "Tag BEACON_PAYMENT_ITEMS"),
            () -> assertTrue(new ItemStack(Items.COAL).is(ItemTags.COALS), "Tag COALS"),
            () -> assertTrue(new ItemStack(Items.CHARCOAL).is(ItemTags.COALS), "Tag COALS")
        );
        helper.succeed();
    }

    @GameTest(template = GameTestUtil.EMPTY_STRUCTURE)
    public void loadMarker(GameTestHelper helper) throws IOException {
        JsonObject jsonObject;
        try (var stream = getClass().getResourceAsStream("/data/quarryplus/recipes/flex_marker_workbench.json");
             var reader = new InputStreamReader(Objects.requireNonNull(stream))) {
            jsonObject = GsonHelper.parse(reader);
        }
        var context = GameTestUtil.getContext(helper);
        var recipe = assertDoesNotThrow(() -> managerFromJson(new ResourceLocation(QuarryPlus.modID, "flex_marker_workbench"), jsonObject, context));
        assertAll(
            () -> assertTrue(recipe instanceof WorkbenchRecipe),
            () -> assertTrue(ItemStack.isSameItemSameTags(recipe.getResultItem(helper.getLevel().registryAccess()), new ItemStack(Holder.BLOCK_FLEX_MARKER)))
        );
        var inputs = ((WorkbenchRecipe) recipe).inputs().stream().flatMap(i -> i.stackList().stream()).toList();
        assertAll(
            () -> match(inputs, Tags.Items.INGOTS_GOLD, 7),
            () -> match(inputs, Tags.Items.INGOTS_IRON, 8),
            () -> match(inputs, Tags.Items.DUSTS_GLOWSTONE, 4),
            () -> match(inputs, Tags.Items.DUSTS_REDSTONE, 12),
            () -> match(inputs, Items.LAPIS_LAZULI, 12),
            () -> match(inputs, Items.REDSTONE_TORCH, 4),
            () -> assertEquals(6, inputs.size())
        );
        helper.succeed();
    }

    private static Recipe<?> managerFromJson(ResourceLocation location, JsonObject jsonObject, ICondition.IContext context) {
        return Try.call(() -> RecipeManager.class.getDeclaredMethod("fromJson", ResourceLocation.class, JsonObject.class))
            .andThenTry(m -> ReflectionSupport.invokeMethod(m, null, location, jsonObject))
            .andThenTry(RecipeHolder.class::cast)
            .andThenTry(RecipeHolder::value)
            .andThenTry(Recipe.class::cast)
            .getOrThrow(RuntimeException::new);
    }

    @GameTest(template = GameTestUtil.EMPTY_STRUCTURE)
    public void loadWithoutType(GameTestHelper helper) {
        var context = GameTestUtil.getContext(helper);
        JsonObject object = GsonHelper.parse("""
            {
              "type": "quarryplus:workbench_recipe",
              "id": "quarryplus:cheat_diamond2",
              "ingredients": [
                {
                  "item": "minecraft:stone",
                  "count": 130
                },
                {
                  "item": "minecraft:coal",
                  "count": 256
                }
              ],
              "energy": 100.0,
              "result": {
                "item": "diamond",
                "count": 1
              }
            }""");
        var r1 = managerFromJson(new ResourceLocation("quarryplus:cheat_diamond2"), object, context);
        var r2 = WorkbenchRecipe.SERIALIZER.fromJson(object, context);
        assertEquals(r1, r2);

        var inputs = r2.inputs().stream().flatMap(i -> i.stackList().stream()).toList();
        assertAll(
            () -> match(inputs, Items.STONE, 130),
            () -> match(inputs, Items.COAL, 256),
            () -> match(List.of(r2.getResultItem(helper.getLevel().registryAccess())), Items.DIAMOND, 1),
            () -> assertEquals(2, inputs.size()),
            () -> assertEquals(100L * PowerTile.ONE_FE, r2.getRequiredEnergy())
        );
        helper.succeed();
    }

    static void match(List<ItemStack> items, TagKey<Item> tag, int count) {
        var stack = items.stream().filter(i -> i.is(tag)).findFirst();
        assertTrue(stack.isPresent(), "%s is not found in inputs(%s)".formatted(tag, stack));
        assertEquals(count, stack.map(ItemStack::getCount).orElse(-1), "%s count must be equal.".formatted(tag));
    }

    static void match(List<ItemStack> items, Item item, int count) {
        var stack = items.stream().filter(i -> i.is(item)).findFirst();
        assertTrue(stack.isPresent(), "%s is not found in inputs(%s)".formatted(item, stack));
        assertEquals(count, stack.map(ItemStack::getCount).orElse(-1), "%s count must be equal.".formatted(item));
    }
}
