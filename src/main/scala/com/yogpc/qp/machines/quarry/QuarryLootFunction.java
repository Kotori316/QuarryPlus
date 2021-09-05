package com.yogpc.qp.machines.quarry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.yogpc.qp.Holder;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class QuarryLootFunction extends LootItemConditionalFunction {
    public static final Serializer<QuarryLootFunction> SERIALIZER = new QuarryLootFunctionSerializer();
    public static final String NAME = "drop_function_quarry";

    protected QuarryLootFunction(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        var blockEntity = context.getParam(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof TileQuarry quarry) {
            process(stack, quarry);
        }
        return stack;
    }

    @Override
    public LootItemFunctionType getType() {
        return Holder.QUARRY_LOOT_TYPE;
    }

    static void process(ItemStack stack, TileQuarry quarry) {
        var tileDataForItem = quarry.getTileDataForItem();
        if (!tileDataForItem.isEmpty())
            stack.addTagElement(BlockItem.BLOCK_ENTITY_TAG, tileDataForItem);
    }

    public static LootItemConditionalFunction.Builder<?> builder() {
        return LootItemConditionalFunction.simpleBuilder(QuarryLootFunction::new);
    }
}

class QuarryLootFunctionSerializer extends LootItemConditionalFunction.Serializer<QuarryLootFunction> {

    @Override
    public QuarryLootFunction deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
        return new QuarryLootFunction(conditions);
    }
}
