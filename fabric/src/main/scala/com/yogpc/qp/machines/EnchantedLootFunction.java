package com.yogpc.qp.machines;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class EnchantedLootFunction extends LootItemConditionalFunction {
    public static final Codec<EnchantedLootFunction> SERIALIZER = RecordCodecBuilder.create(instance ->
        commonFields(instance).apply(instance, EnchantedLootFunction::new));

    protected EnchantedLootFunction(List<LootItemCondition> conditions) {
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
