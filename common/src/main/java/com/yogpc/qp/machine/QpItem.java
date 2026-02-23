package com.yogpc.qp.machine;

import com.yogpc.qp.InCreativeTabs;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.config.EnableMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public abstract class QpItem extends Item implements InCreativeTabs {
    public final Identifier name;

    public QpItem(Properties properties, String name) {
        super(properties.setId(QuarryPlus.itemKey(name)));
        this.name = Identifier.fromNamespaceAndPath(QuarryPlus.modID, name);
    }

    public boolean isEnabled() {
        return PlatformAccess.config().enableMap().enabled(name.getPath());
    }

    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipDisplay, consumer, tooltipFlag);
        if (!isEnabled()) {
            MutableComponent message;
            if (EnableMap.getDefaultValue(name.getPath()) == EnableMap.EnableOrNot.ALWAYS_OFF) {
                message = Component.translatable("quarryplus.chat.not_available_platform", PlatformAccess.getAccess().platformName());
            } else {
                message = Component.translatable("quarryplus.chat.disable_item_message");
            }
            consumer.accept(message.withStyle(ChatFormatting.RED));
        }
    }
}
