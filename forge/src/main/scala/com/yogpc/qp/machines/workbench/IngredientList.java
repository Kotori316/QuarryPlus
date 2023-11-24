package com.yogpc.qp.machines.workbench;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.yogpc.qp.utils.MapMulti;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class IngredientList implements Predicate<ItemStack> {
    private final List<IngredientWithCount> ingredientList;

    public IngredientList(List<IngredientWithCount> ingredientList) {
        this.ingredientList = ingredientList;
    }

    public IngredientList(IngredientWithCount ingredient) {
        this(List.of(ingredient));
    }

    @Override
    public boolean test(ItemStack stack) {
        return ingredientList.stream().anyMatch(t -> t.test(stack));
    }

    boolean shrink(ItemStack stack) {
        return ingredientList.stream().anyMatch(t -> t.shrink(stack));
    }

    boolean invalid() {
        return ingredientList.stream().allMatch(t -> t.ingredient().isEmpty());
    }

    public List<ItemStack> stackList() {
        return this.ingredientList.stream()
            .flatMap(i -> i.stackList().stream())
            .toList();
    }

    public JsonElement toJson() {
        if (ingredientList.size() == 1) {
            return ingredientList.get(0).toJson();
        } else {
            return ingredientList.stream()
                .map(IngredientWithCount::toJson)
                .collect(MapMulti.jsonArrayCollector());
        }
    }

    public static IngredientList fromJson(JsonElement jsonElement) {
        if (jsonElement instanceof JsonArray array) {
            return new IngredientList(IngredientWithCount.getSeq(array));
        } else if (jsonElement instanceof JsonObject object) {
            return new IngredientList(new IngredientWithCount(object));
        } else {
            throw new IllegalArgumentException("Invalid Json type: " + jsonElement.getClass() + " value=" + jsonElement);
        }
    }

    public void toPacket(FriendlyByteBuf buffer) {
        buffer.writeVarInt(ingredientList.size());
        for (IngredientWithCount ingredient : ingredientList) {
            ingredient.toPacket(buffer);
        }
    }

    public static IngredientList fromPacket(FriendlyByteBuf buffer) {
        var size = buffer.readVarInt();
        var list = Stream.generate(() -> IngredientWithCount.fromPacket(buffer)).limit(size).toList();
        return new IngredientList(list);
    }
}
