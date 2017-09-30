package com.yogpc.qp.version;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class VersionUtil {

    public static ItemStack empty() {
        return QuarryPlus.DIFF.empty();
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
}
