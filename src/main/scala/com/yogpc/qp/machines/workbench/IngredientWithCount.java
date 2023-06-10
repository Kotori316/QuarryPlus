package com.yogpc.qp.machines.workbench;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

public record IngredientWithCount(Ingredient ingredient, int count) implements Predicate<ItemStack> {
    public IngredientWithCount(JsonObject jsonObject) {
        this(CraftingHelper.getIngredient(modifyCount(jsonObject), false), GsonHelper.getAsInt(jsonObject, "count"));
    }

    public IngredientWithCount(ItemStack stack) {
        this(Ingredient.of(ItemHandlerHelper.copyStackWithSize(stack, 1)), stack.getCount());
    }

    @Override
    public boolean test(ItemStack stack) {
        return ingredient.test(stack) && stack.getCount() >= count;
    }

    boolean shrink(ItemStack stack) {
        if (test(stack)) {
            stack.shrink(count);
            return true;
        } else {
            return false;
        }
    }

    public List<ItemStack> stackList() {
        return Arrays.stream(ingredient.getItems())
                .filter(Predicate.not(ItemStack::isEmpty))
                .map(stack -> ItemHandlerHelper.copyStackWithSize(stack, count))
                .toList();
    }

    public JsonElement toJson() {
        var obj = ingredient.toJson();
        if (obj instanceof JsonArray jsonArray) {
            jsonArray.forEach(e -> e.getAsJsonObject().addProperty("count", count));
        } else if (obj instanceof JsonObject jsonObject) {
            jsonObject.addProperty("count", count);
        }
        return obj;
    }

    public void toPacket(FriendlyByteBuf buf) {
        ingredient.toNetwork(buf);
        buf.writeInt(count);
    }

    public static List<IngredientWithCount> getSeq(JsonElement jsonElement) {
        if (jsonElement instanceof JsonArray jsonArray) {
            return StreamSupport.stream(jsonArray.spliterator(), false)
                    .map(JsonElement::getAsJsonObject)
                    .map(IngredientWithCount::new)
                    .toList();
        } else {
            return List.of(new IngredientWithCount(jsonElement.getAsJsonObject()));
        }
    }

    public static IngredientWithCount fromPacket(FriendlyByteBuf buf) {
        var ingredient = Ingredient.fromNetwork(buf);
        var count = buf.readInt();
        return new IngredientWithCount(ingredient, count);
    }

    static JsonObject modifyCount(JsonObject object) {
        if (object.has("count")) {
            var clone = new JsonObject();
            object.entrySet().forEach(e -> clone.add(e.getKey(), e.getValue()));
            clone.remove("count");
            return clone;
        } else {
            return object;
        }
    }
}
