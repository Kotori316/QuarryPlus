package com.yogpc.qp.version;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;

public interface VersionDiff {
    ItemStack empty();

    ItemStack fromNBTTag(NBTTagCompound compound);

    boolean isEmpty(ItemStack stack);

    int getCount(ItemStack stack);

    void setCount(ItemStack stack, int newSize);

    default void shrink(ItemStack stack, int size) {
        setCount(stack, getCount(stack) - size);
    }

    default void grow(ItemStack stack, int size) {
        setCount(stack, getCount(stack) + size);
    }

    default boolean nonEmpty(ItemStack stack) {
        return !isEmpty(stack);
    }

    void sendWindowProperty(IContainerListener listener, Container containerIn, int varToUpdate, int newValue);

    void onTake(Slot slot, EntityPlayer thePlayer, ItemStack stack);

    Iterator<NBTBase> getIterator(NBTTagList list);

    Function<NBTBase, NBTTagCompound> NBT_TAG_COMPOUND_FUNCTION = NBTTagCompound.class::cast;

    default Stream<NBTTagCompound> nbtListStream(NBTTagList list) {
        if (list == null) {
            return Stream.empty();
        } else {
            return StreamSupport.stream(Spliterators.spliterator(getIterator(list), list.tagCount(), Spliterator.ORDERED),
                false).map(NBT_TAG_COMPOUND_FUNCTION);
        }
    }

    default boolean changeAdvPumpState() {
        return false;
    }

    void sendMessage(EntityPlayer player, ITextComponent component);
}
