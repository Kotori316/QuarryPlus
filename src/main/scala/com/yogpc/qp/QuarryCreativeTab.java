package com.yogpc.qp;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

class QuarryCreativeTab extends CreativeModeTab {
    private final List<ItemLike> items = new ArrayList<>();

    QuarryCreativeTab() {
        super(QuarryPlus.modID);
    }

    @Override
    public ItemStack makeIcon() {
        return new ItemStack(Holder.BLOCK_QUARRY);
    }

    void addItem(ItemLike item) {
        this.items.add(item);
    }

    @Override
    public void fillItemList(NonNullList<ItemStack> items) {
        this.items.stream().map(ItemLike::asItem).forEach(item ->
            item.fillItemCategory(this, items));
    }
}
