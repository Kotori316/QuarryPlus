package com.yogpc.qp.machines.module;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.QPItem;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class RepeatTickModuleItem extends QPItem implements QuarryModuleProvider.Item {
    public static final String NAME = "repeat_tick_module";

    public RepeatTickModuleItem() {
        super(new Properties().tab(Holder.TAB));
        setRegistryName(QuarryPlus.modID, NAME);
    }

    @Override
    public QuarryModule getModule(@NotNull ItemStack stack) {
        return new RepeatTickModule(stack.getCount());
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> tooltips, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, tooltips, pIsAdvanced);
        tooltips.add(new TranslatableComponent("quarryplus.tooltip.repeat_tick_module"));
    }

    public record RepeatTickModule(int stackSize) implements QuarryModule {
        @Override
        public ResourceLocation moduleId() {
            return Holder.ITEM_REPEAT_MODULE.getRegistryName();
        }
    }
}
