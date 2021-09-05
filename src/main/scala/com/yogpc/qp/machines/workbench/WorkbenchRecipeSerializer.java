package com.yogpc.qp.machines.workbench;

import java.util.Map;
import java.util.Objects;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class WorkbenchRecipeSerializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<WorkbenchRecipe> {
    private final Map<String, PacketSerialize<? extends WorkbenchRecipe>> serializeMap;

    WorkbenchRecipeSerializer() {
        setRegistryName(WorkbenchRecipe.recipeLocation);
        serializeMap = Map.of(
            "default", new IngredientRecipeSerialize()
        );
    }

    @Override
    public WorkbenchRecipe fromJson(ResourceLocation id, JsonObject jsonObject) {
        var subType = GsonHelper.getAsString(jsonObject, "subType", "default");
        return serializeMap.get(subType).fromJson(id, jsonObject);
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
    public WorkbenchRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
        var subType = buffer.readUtf();
        return serializeMap.get(subType).fromPacket(id, buffer);
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
        T fromJson(ResourceLocation id, JsonObject jsonObject);

        JsonObject toJson(JsonObject jsonObject, T recipe);

        T fromPacket(ResourceLocation id, FriendlyByteBuf buffer);

        void toPacket(FriendlyByteBuf buffer, T recipe);

        static JsonObject toJson(ItemStack stack) {
            var o = new JsonObject();
            o.addProperty("item", Objects.requireNonNull(stack.getItem().getRegistryName()).toString());
            o.addProperty("count", stack.getCount());
            if (stack.getTag() != null)
                o.addProperty("nbt", stack.getTag().toString());
            return o;
        }
    }
}
