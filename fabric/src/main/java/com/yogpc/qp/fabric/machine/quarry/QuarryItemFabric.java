package com.yogpc.qp.fabric.machine.quarry;

import com.yogpc.qp.QuarryDataComponents;
import com.yogpc.qp.machine.QpBlock;
import com.yogpc.qp.machine.quarry.QuarryItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public final class QuarryItemFabric extends QuarryItem {
    public QuarryItemFabric(QpBlock block) {
        super(block);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, tooltipContext, tooltipDisplay, consumer, tooltipFlag);
        if (itemStack.getOrDefault(QuarryDataComponents.QUARRY_REMOVE_BEDROCK_COMPONENT, Boolean.FALSE)) {
            consumer.accept(Component.translatable("quarryplus.tooltip.remove_bedrock_on"));
        }
    }
}
