package com.yogpc.qp.machines.quarry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class QuarryLootFunction extends LootItemConditionalFunction {
    public static final Codec<QuarryLootFunction> SERIALIZER = RecordCodecBuilder.create(instance ->
        commonFields(instance).apply(instance, QuarryLootFunction::new));

    protected QuarryLootFunction(List<LootItemCondition> conditions) {
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
        return QuarryPlus.ModObjects.ENCHANTED_LOOT_TYPE;
    }

    static void process(ItemStack stack, TileQuarry quarry) {
        CompoundTag tileDataForItem = quarry.getTileDataForItem();
        if (!tileDataForItem.isEmpty())
            BlockItem.setBlockEntityData(stack, quarry.getType(), tileDataForItem);
    }
}
