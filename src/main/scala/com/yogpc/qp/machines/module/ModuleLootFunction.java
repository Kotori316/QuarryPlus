package com.yogpc.qp.machines.module;

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

public class ModuleLootFunction extends LootItemConditionalFunction {
    public static final Serializer<ModuleLootFunction> SERIALIZER = new ModuleLootFunctionSerializer();
    public static final String NAME = "drop_function_module";

    protected ModuleLootFunction(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        var blockEntity = context.getParam(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof ModuleInventory.HasModuleInventory moduleInventory) {
            process(stack, moduleInventory);
        }
        return stack;
    }

    @Override
    public LootItemFunctionType getType() {
        return Holder.MODULE_LOOT_TYPE;
    }

    public static void process(ItemStack stack, ModuleInventory.HasModuleInventory holder) {
        var moduleInventory = holder.getModuleInventory();
        if (!moduleInventory.isEmpty()) {
            var blockTag = stack.getOrCreateTagElement("BlockEntityTag");
            blockTag.put("moduleInventory", moduleInventory.serializeNBT());
        }
    }

    public static Builder<?> builder() {
        return LootItemConditionalFunction.simpleBuilder(ModuleLootFunction::new);
    }
}

class ModuleLootFunctionSerializer extends LootItemConditionalFunction.Serializer<ModuleLootFunction> {

    @Override
    public ModuleLootFunction deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
        return new ModuleLootFunction(conditions);
    }
}
