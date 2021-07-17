package com.yogpc.qp.machines;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.util.JsonSerializer;

public class EnchantedLootFunction extends ConditionalLootFunction {
    public static final JsonSerializer<EnchantedLootFunction> SERIALIZER = new EnchantedLootFunctionSerializer();

    protected EnchantedLootFunction(LootCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack process(ItemStack stack, LootContext context) {
        var blockEntity = context.get(LootContextParameters.BLOCK_ENTITY);
        if (blockEntity instanceof EnchantmentLevel.HasEnchantments hasEnchantments) {
            for (EnchantmentLevel enchantmentLevel : hasEnchantments.getEnchantments()) {
                stack.addEnchantment(enchantmentLevel.enchantment(), enchantmentLevel.level());
            }
        }
        return stack;
    }

    @Override
    public LootFunctionType getType() {
        return QuarryPlus.ModObjects.ENCHANTED_LOOT_TYPE;
    }
}

class EnchantedLootFunctionSerializer extends ConditionalLootFunction.Serializer<EnchantedLootFunction> {

    @Override
    public EnchantedLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
        return new EnchantedLootFunction(conditions);
    }
}
