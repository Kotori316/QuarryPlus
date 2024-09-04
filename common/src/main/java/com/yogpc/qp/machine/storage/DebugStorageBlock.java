package com.yogpc.qp.machine.storage;

import com.yogpc.qp.machine.QpBlock;
import com.yogpc.qp.machine.QpEntityBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public final class DebugStorageBlock extends QpEntityBlock {
    public static final String NAME = "debug_storage";

    public DebugStorageBlock() {
        super(Properties.of().noLootTable(), NAME, b -> new BlockItem(b, new Item.Properties()));
    }

    @Override
    protected QpBlock createBlock(Properties properties) {
        return new DebugStorageBlock();
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.literal("Insertion Only. No extraction"));
        tooltipComponents.add(Component.literal("Just for debug").withStyle(ChatFormatting.RED));
    }
}
