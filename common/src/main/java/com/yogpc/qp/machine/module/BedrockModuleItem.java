package com.yogpc.qp.machine.module;

import com.yogpc.qp.machine.QpItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public final class BedrockModuleItem extends QpItem implements QuarryModuleProvider.Item {
    public static final String NAME = "remove_bedrock_module";

    public BedrockModuleItem() {
        super(new Properties().fireResistant(), NAME);
    }

    @Override
    public QuarryModule getModule(ItemStack stack) {
        return QuarryModule.Constant.BEDROCK;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipDisplay, consumer, tooltipFlag);
        consumer.accept(Component.translatable("quarryplus.chat.bedrock_module_description"));
    }
}
