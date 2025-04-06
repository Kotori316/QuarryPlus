package com.yogpc.qp.machine;

import com.yogpc.qp.InCreativeTabs;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public abstract class QpItem extends Item implements InCreativeTabs {
    public final ResourceLocation name;

    public QpItem(Properties properties, String name) {
        super(properties.setId(QuarryPlus.itemKey(name)));
        this.name = ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, name);
    }

    public boolean isEnabled() {
        return PlatformAccess.config().enableMap().enabled(name.getPath());
    }

    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipDisplay, consumer, tooltipFlag);
        if (!isEnabled()) {
            consumer.accept(
                Component.translatable("quarryplus.chat.disable_item_message").withStyle(ChatFormatting.RED)
            );
        }
    }
}
