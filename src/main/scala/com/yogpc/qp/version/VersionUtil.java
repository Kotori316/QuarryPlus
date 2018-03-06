package com.yogpc.qp.version;

import java.util.stream.Stream;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class VersionUtil {

    public static ItemStack empty() {
        return QuarryPlus.DIFF.empty();
    }

    public static ItemStack fromNBTTag(NBTTagCompound compound) {
        return QuarryPlus.DIFF.fromNBTTag(compound);
    }

    public static void setCount(ItemStack stack, int newSize) {
        QuarryPlus.DIFF.setCount(stack, newSize);
    }

    public static int getCount(ItemStack stack) {
        return QuarryPlus.DIFF.getCount(stack);
    }

    public static void shrink(ItemStack stack, int size) {
        QuarryPlus.DIFF.shrink(stack, size);
    }

    public static void grow(ItemStack stack, int size) {
        QuarryPlus.DIFF.grow(stack, size);
    }

    public static boolean isEmpty(ItemStack stack) {
        return QuarryPlus.DIFF.isEmpty(stack);
    }

    public static boolean nonEmpty(ItemStack stack) {
        return QuarryPlus.DIFF.nonEmpty(stack);
    }

    public static void sendWindowProperty(IContainerListener listener, Container containerIn, int varToUpdate, int newValue) {
        QuarryPlus.DIFF.sendWindowProperty(listener, containerIn, varToUpdate, newValue);
    }

    public static void onTake(Slot slot, EntityPlayer thePlayer, ItemStack stack) {
        QuarryPlus.DIFF.onTake(slot, thePlayer, stack);
    }

    public static Stream<NBTTagCompound> nbtListStream(NBTTagList list) {
        return QuarryPlus.DIFF.nbtListStream(list);
    }

    public static boolean changeAdvPumpState() {
        return QuarryPlus.DIFF.changeAdvPumpState();
    }

    public static ResourceLocation getRegistryName(IForgeRegistryEntry<?> entry) {
        ResourceLocation registryName = entry.getRegistryName();
        if (registryName != null) {
            return registryName;
        } else {
            return new ResourceLocation("Unknown:Dummy");
        }
    }
}
