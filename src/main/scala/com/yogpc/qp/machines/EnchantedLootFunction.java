package com.yogpc.qp.machines;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class EnchantedLootFunction extends LootItemConditionalFunction {
    public static final net.minecraft.world.level.storage.loot.Serializer<EnchantedLootFunction> SERIALIZER = new EnchantedLootFunctionSerializer();

    protected EnchantedLootFunction(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        var blockEntity = context.getParam(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof EnchantmentLevel.HasEnchantments hasEnchantments) {
            process(stack, hasEnchantments);
        }
        return stack;
    }

    @Override
    public LootItemFunctionType getType() {
        return QuarryPlus.ModObjects.ENCHANTED_LOOT_TYPE;
    }

    public static void process(ItemStack stack, EnchantmentLevel.HasEnchantments hasEnchantments) {
        for (EnchantmentLevel enchantmentLevel : hasEnchantments.getEnchantments()) {
            stack.enchant(enchantmentLevel.enchantment(), enchantmentLevel.level());
        }
    }

}

class EnchantedLootFunctionSerializer extends LootItemConditionalFunction.Serializer<EnchantedLootFunction> {

    @Override
    public EnchantedLootFunction deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
        return new EnchantedLootFunction(conditions);
    }
}
