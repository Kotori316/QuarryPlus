package com.yogpc.qp.machines.workbench;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.conditions.ConditionCodec;
import net.minecraftforge.common.crafting.conditions.ICondition;

import java.util.Map;

public class WorkbenchRecipeSerializer implements RecipeSerializer<WorkbenchRecipe> {
    public static final Codec<WorkbenchRecipe> CODEC = new RecipeCodec();
    private final Map<String, PacketSerialize<? extends WorkbenchRecipe>> serializeMap;

    WorkbenchRecipeSerializer() {
        serializeMap = Map.of(
            "default", new IngredientRecipeSerialize()
        );
    }

    public WorkbenchRecipe fromJson(JsonObject recipeJson, ICondition.IContext context) {
        var subType = GsonHelper.getAsString(recipeJson, "subType", "default");
        return serializeMap.get(subType).fromJson(recipeJson, context);
    }

    public JsonObject toJson(WorkbenchRecipe recipe, JsonObject o) {
        o.addProperty("subType", recipe.getSubTypeName());
        return toJson(o, serializeMap.get(recipe.getSubTypeName()), recipe);
    }

    @SuppressWarnings("unchecked")
    private static <T extends WorkbenchRecipe> JsonObject toJson(JsonObject object, PacketSerialize<T> serialize, WorkbenchRecipe recipe) {
        return serialize.toJson(object, (T) recipe);
    }

    @Override
    public WorkbenchRecipe fromNetwork( FriendlyByteBuf buffer) {
        var subType = buffer.readUtf();
        return serializeMap.get(subType).fromPacket(buffer);
    }

    @Override
    public Codec<WorkbenchRecipe> codec() {
        return CODEC;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, WorkbenchRecipe recipe) {
        buffer.writeUtf(recipe.getSubTypeName());
        toNetwork(serializeMap.get(recipe.getSubTypeName()), buffer, recipe);
    }

    @SuppressWarnings("unchecked")
    private static <T extends WorkbenchRecipe> void toNetwork(PacketSerialize<T> serialize, FriendlyByteBuf buffer, WorkbenchRecipe recipe) {
        serialize.toPacket(buffer, (T) recipe);
    }

    interface PacketSerialize<T extends WorkbenchRecipe> {
        T fromJson(JsonObject jsonObject, ICondition.IContext context);

        JsonObject toJson(JsonObject jsonObject, T recipe);

        T fromPacket(FriendlyByteBuf buffer);

        void toPacket(FriendlyByteBuf buffer, T recipe);
    }

    private static class RecipeCodec implements Codec<WorkbenchRecipe> {
        @Override
        public <T> DataResult<Pair<WorkbenchRecipe, T>> decode(DynamicOps<T> ops, T input) {
            try {
                ICondition.IContext context = ConditionCodec.getContext(ops);
                JsonObject jsonObject = ops.convertTo(JsonOps.INSTANCE, input).getAsJsonObject();
                return DataResult.success(Pair.of(WorkbenchRecipe.SERIALIZER.fromJson(jsonObject, context), ops.empty()));
            } catch (RuntimeException e) {
                return DataResult.error(e::getMessage);
            }
        }

        @Override
        public <T> DataResult<T> encode(WorkbenchRecipe input, DynamicOps<T> ops, T prefix) {
            var json = WorkbenchRecipe.SERIALIZER.toJson(input, new JsonObject());
            var tMap = JsonOps.INSTANCE.convertTo(ops, json);
            return ops.getMap(tMap)
                .flatMap(m -> ops.mergeToMap(prefix, m));
        }
    }
}
