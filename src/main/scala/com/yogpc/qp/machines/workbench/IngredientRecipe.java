package com.yogpc.qp.machines.workbench;

import com.google.gson.JsonObject;
import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.utils.MapMulti;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

public class IngredientRecipe extends WorkbenchRecipe {
    private final List<IngredientList> input;

    public IngredientRecipe(ItemStack output, long energy, boolean showInJEI,
                            List<IngredientList> input) {
        super(output, energy, showInJEI);
        this.input = input;
    }

    @Override
    public List<IngredientList> inputs() {
        return input;
    }

    @Override
    protected String getSubTypeName() {
        return "default";
    }

    @Override
    protected ItemStack getOutput(List<ItemStack> inventory, RegistryAccess access) {
        return super.getResultItem(access).copy();
    }

    @Override
    public boolean hasContent() {
        return !this.output.isEmpty() && !this.input.isEmpty() && this.input.stream().noneMatch(IngredientList::invalid);
    }
}

class IngredientRecipeSerialize implements WorkbenchRecipeSerializer.PacketSerialize<IngredientRecipe> {

    @Override
    public IngredientRecipe fromJson(JsonObject jsonObject, ICondition.IContext context) {
        var result = CraftingHelper.getItemStack(jsonObject.getAsJsonObject("result"), true);
        long energy = (long) (GsonHelper.getAsDouble(jsonObject, "energy", 1000) * PowerTile.ONE_FE);
        var showInJei = GsonHelper.getAsBoolean(jsonObject, "showInJEI", true);
        List<IngredientList> input;
        if (jsonObject.get("ingredients").isJsonObject()) {
            input = List.of(IngredientList.fromJson(jsonObject.get("ingredients")));
        } else if (jsonObject.get("ingredients").isJsonArray()) {
            input = StreamSupport.stream(jsonObject.get("ingredients").getAsJsonArray().spliterator(), false)
                .map(IngredientList::fromJson)
                .toList();
        } else {
            throw new IllegalArgumentException("Bad Json type of ingredients. " + jsonObject.get("ingredients"));
        }
        return new IngredientRecipe(result, energy, showInJei, input);
    }

    @Override
    public JsonObject toJson(JsonObject jsonObject, IngredientRecipe recipe) {
        jsonObject.add("result", WorkbenchRecipeSerializer.PacketSerialize.toJson(recipe.output));
        jsonObject.addProperty("energy", (double) recipe.getRequiredEnergy() / PowerTile.ONE_FE);
        jsonObject.addProperty("showInJEI", recipe.showInJEI());
        jsonObject.add("ingredients",
            recipe.inputs().stream().map(IngredientList::toJson).collect(MapMulti.jsonArrayCollector()));
        return jsonObject;
    }

    @Override
    public IngredientRecipe fromPacket(FriendlyByteBuf buffer) {
        var output = buffer.readItem();
        var energy = buffer.readLong();
        var showInJei = buffer.readBoolean();
        var inputSize = buffer.readVarInt();
        var input = IntStream.range(0, inputSize)
            .mapToObj(i -> IngredientList.fromPacket(buffer))
            .toList();
        return new IngredientRecipe(output, energy, showInJei, input);
    }

    @Override
    public void toPacket(FriendlyByteBuf buffer, IngredientRecipe recipe) {
        buffer.writeItemStack(recipe.output, false);
        buffer.writeLong(recipe.getRequiredEnergy()).writeBoolean(recipe.showInJEI());

        buffer.writeVarInt(recipe.inputs().size());
        for (IngredientList input : recipe.inputs()) {
            input.toPacket(buffer);
        }
    }
}
