package com.yogpc.qp.machines.module;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.yogpc.qp.Holder;
import com.yogpc.qp.machines.ItemConverter;
import com.yogpc.qp.machines.ItemKey;
import com.yogpc.qp.utils.MapMulti;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

public final class FilterModule implements QuarryModule {

    @Nullable
    private final ListTag listTag;

    public FilterModule(List<ItemKey> itemKeys) {
        this(getFromItemKeys(itemKeys.stream()));
    }

    public FilterModule(@Nullable ListTag tag) {
        this.listTag = tag;
    }

    public static List<ItemKey> getFromTag(@Nullable ListTag tag) {
        if (tag == null || tag.isEmpty()) return List.of();
        return tag.stream()
            .mapMulti(MapMulti.cast(CompoundTag.class))
            .map(ItemKey::fromNbt)
            .distinct()
            .toList();
    }

    public static ListTag getFromItems(List<ItemStack> stacks) {
        return getFromItemKeys(stacks.stream().map(ItemKey::new));
    }

    static ListTag getFromItemKeys(Stream<ItemKey> keyStream) {
        return keyStream.map(ItemKey::createNbt).collect(Collectors.toCollection(ListTag::new));
    }

    @Override
    public ResourceLocation moduleId() {
        return Holder.ITEM_FILTER_MODULE.getRegistryName();
    }

    public ItemConverter createConverter() {
        return ItemConverter.voidConverter(this.getItemKeys());
    }

    @VisibleForTesting
    List<ItemKey> getItemKeys() {
        return getFromTag(this.listTag);
    }

    @Override
    public String toString() {
        int size = this.listTag == null ? 0 : this.listTag.size();
        return "FilterModule{size=" + size + "}";
    }
}
