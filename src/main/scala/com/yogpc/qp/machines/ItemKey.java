package com.yogpc.qp.machines;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

public record ItemKey(Item item, @Nullable NbtCompound nbt) {
    public ItemKey(ItemStack stack) {
        this(stack.getItem(), stack.getNbt());
    }

    public NbtCompound createNbt(long itemCount) {
        var tag = new NbtCompound();
        tag.putString("item", getId().toString());
        if (nbt != null)
            tag.put("tag", nbt);
        tag.putLong("count", itemCount);
        return tag;
    }

    static ItemKey fromNbt(NbtCompound tag) {
        var item = Registry.ITEM.get(new Identifier(tag.getString("item")));
        var nbt = tag.contains("tag") ? tag.getCompound("tag") : null;
        return new ItemKey(item, nbt);
    }

    public ItemStack toStack(int count) {
        var stack = new ItemStack(item, count);
        stack.setNbt(nbt);
        return stack;
    }

    public Identifier getId() {
        return Registry.ITEM.getId(item);
    }
}
