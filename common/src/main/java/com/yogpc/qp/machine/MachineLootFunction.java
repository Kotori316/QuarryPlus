package com.yogpc.qp.machine;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yogpc.qp.PlatformAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public final class MachineLootFunction extends LootItemConditionalFunction {
    public static final String NAME = "machine_loot_function";
    public static final MapCodec<MachineLootFunction> SERIALIZER = RecordCodecBuilder.mapCodec(instance ->
        commonFields(instance).apply(instance, MachineLootFunction::new));

    private MachineLootFunction(List<LootItemCondition> conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        var blockEntity = context.getParam(LootContextParams.BLOCK_ENTITY);
        process(stack, blockEntity);
        return stack;
    }

    @Override
    public LootItemFunctionType<? extends MachineLootFunction> getType() {
        return PlatformAccess.getAccess().registerObjects().machineLootFunction().get();
    }

    public static void process(ItemStack stack, BlockEntity entity) {
        stack.applyComponents(entity.collectComponents());
    }

    public static LootItemConditionalFunction.Builder<?> builder() {
        return LootItemConditionalFunction.simpleBuilder(MachineLootFunction::new);
    }
}
