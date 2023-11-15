package com.yogpc.qp.machines;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public record ItemKey(Item item, @Nullable CompoundTag nbt) {
    public static final ItemKey EMPTY_KEY = new ItemKey(ItemStack.EMPTY);

    public ItemKey(ItemStack stack) {
        this(stack.getItem(), stack.getTag());
    }

    public CompoundTag createNbt(long itemCount) {
        var tag = new CompoundTag();
        tag.putString("item", getId().toString());
        if (nbt != null)
            tag.put("tag", nbt);
        tag.putLong("count", itemCount);
        return tag;
    }

    public CompoundTag createNbt() {
        return createNbt(1);
    }

    public static ItemKey fromNbt(CompoundTag tag) {
        var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(tag.getString("item")));
        var nbt = tag.contains("tag") ? tag.getCompound("tag") : null;
        return new ItemKey(item, nbt);
    }

    public ItemStack toStack(int count) {
        var stack = new ItemStack(item, count);
        stack.setTag(nbt);
        return stack;
    }

    public ResourceLocation getId() {
        return ForgeRegistries.ITEMS.getKey(item);
    }
}
