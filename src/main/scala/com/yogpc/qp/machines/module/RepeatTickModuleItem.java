package com.yogpc.qp.machines.module;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.QPItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

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

    public record RepeatTickModule(int stackSize) implements QuarryModule {
        @Override
        public ResourceLocation moduleId() {
            return Holder.ITEM_REPEAT_MODULE.getRegistryName();
        }
    }
}
