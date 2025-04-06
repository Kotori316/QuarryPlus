package com.yogpc.qp.machine;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class QpBlockItem extends BlockItem {
    private final QpBlock block;

    public QpBlockItem(QpBlock block, Properties properties) {
        super(block, properties.setId(ResourceKey.create(Registries.ITEM, block.name)).useBlockDescriptionPrefix());
        this.block = block;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, tooltipContext, tooltipDisplay, consumer, tooltipFlag);
        List<Component> tooltipComponents = new ArrayList<>();
        this.block.appendHoverText(itemStack, tooltipContext, tooltipComponents, tooltipFlag);
        tooltipComponents.forEach(consumer);
    }
}
