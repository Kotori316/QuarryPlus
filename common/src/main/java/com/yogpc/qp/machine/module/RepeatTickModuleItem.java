package com.yogpc.qp.machine.module;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.QpItem;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

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
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipDisplay, consumer, tooltipFlag);
        consumer.accept(Component.translatable("quarryplus.tooltip.repeat_tick_module"));
    }

    public record RepeatTickModule(int stackSize) implements QuarryModule {
        @Override
        public Identifier moduleId() {
            return Identifier.fromNamespaceAndPath(QuarryPlus.modID, NAME);
        }
    }

    public static final RepeatTickModule ZERO = new RepeatTickModule(0);

    public static Optional<RepeatTickModule> getModule(Collection<QuarryModule> modules) {
        return modules.stream()
            .filter(RepeatTickModule.class::isInstance)
            .map(RepeatTickModule.class::cast)
            .findAny();
    }
}
