package com.yogpc.qp.machines.module;

import java.util.Optional;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.QPItem;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class FilterModuleItem extends QPItem implements QuarryModuleProvider.Item {
    public static final String NAME = "filter_module";
    public static final String KEY_ITEMS = "filter_items";

    public FilterModuleItem() {
        super(new ResourceLocation(QuarryPlus.modID, NAME), new Properties().tab(Holder.TAB));
    }

    @Override
    public QuarryModule getModule(@NotNull ItemStack stack) {
        var tagList = Optional.ofNullable(stack.getTag())
            .map(t -> t.getList(KEY_ITEMS, Tag.TAG_COMPOUND))
            .orElse(null);
        return new FilterModule(tagList);
    }
}
