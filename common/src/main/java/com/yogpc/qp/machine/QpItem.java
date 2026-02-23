package com.yogpc.qp.machine;

import com.yogpc.qp.InCreativeTabs;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.config.EnableMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public abstract class QpItem extends Item implements InCreativeTabs {
    public final ResourceLocation name;

    public QpItem(Properties properties, String name) {
        super(properties);
        this.name = ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, name);
    }

    public boolean isEnabled() {
        return PlatformAccess.config().enableMap().enabled(name.getPath());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        if (!isEnabled()) {
            MutableComponent message;
            if (EnableMap.getDefaultValue(name.getPath()) == EnableMap.EnableOrNot.ALWAYS_OFF) {
                message = Component.translatable("quarryplus.chat.not_available_platform", PlatformAccess.getAccess().platformName());
            } else {
                message = Component.translatable("quarryplus.chat.disable_item_message");
            }
            tooltipComponents.add(message.withStyle(ChatFormatting.RED));
        }
    }
}
