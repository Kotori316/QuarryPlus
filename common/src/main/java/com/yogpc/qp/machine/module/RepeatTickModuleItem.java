package com.yogpc.qp.machine.module;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.QpItem;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class RepeatTickModuleItem extends QpItem implements QuarryModuleProvider.Item {
    public static final String NAME = "repeat_tick_module";

    public RepeatTickModuleItem() {
        super(new Properties(), NAME);
    }

    @Override
    public QuarryModule getModule(@NotNull ItemStack stack) {
        return new RepeatTickModule(stack.getCount());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("quarryplus.tooltip.repeat_tick_module"));
    }

    public record RepeatTickModule(int stackSize) implements QuarryModule {
        @Override
        public ResourceLocation moduleId() {
            return ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, NAME);
        }
    }
}
