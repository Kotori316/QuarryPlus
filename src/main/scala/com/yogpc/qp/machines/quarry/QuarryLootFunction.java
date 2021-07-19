package com.yogpc.qp.machines.quarry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.util.JsonSerializer;

public class QuarryLootFunction extends ConditionalLootFunction {
    public static final JsonSerializer<QuarryLootFunction> SERIALIZER = new QuarryLootFunctionSerializer();

    protected QuarryLootFunction(LootCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack process(ItemStack stack, LootContext context) {
        var blockEntity = context.get(LootContextParameters.BLOCK_ENTITY);
        if (blockEntity instanceof TileQuarry quarry) {
            process(stack, quarry);
        }
        return stack;
    }

    @Override
    public LootFunctionType getType() {
        return QuarryPlus.ModObjects.ENCHANTED_LOOT_TYPE;
    }

    static void process(ItemStack stack, TileQuarry quarry) {
        var tileDataForItem = quarry.getTileDataForItem();
        if (!tileDataForItem.isEmpty())
            stack.setSubNbt(BlockItem.BLOCK_ENTITY_TAG_KEY, tileDataForItem);
    }
}

class QuarryLootFunctionSerializer extends ConditionalLootFunction.Serializer<QuarryLootFunction> {

    @Override
    public QuarryLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
        return new QuarryLootFunction(conditions);
    }
}
