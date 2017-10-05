package com.yogpc.qp.version;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class Diff10 implements VersionDiff {

    private final Field size_Stack;
    private final Method sendProgressBarUpdate_IContainerListener;
    private final Method onPickupFromSlot_Slot;

    @SuppressWarnings("JavaReflectionMemberAccess")
    public Diff10() throws ReflectiveOperationException {
        size_Stack = ItemStack.class.getDeclaredField("stackSize");
        size_Stack.setAccessible(true);
        onPickupFromSlot_Slot = Slot.class.getMethod("onPickupFromSlot", EntityPlayer.class, ItemStack.class);
        sendProgressBarUpdate_IContainerListener = IContainerListener.class.getMethod("sendProgressBarUpdate", Container.class, int.class, int.class);
    }

    @Override
    public ItemStack empty() {
        return null;
    }

    @Override
    public boolean isEmpty(ItemStack stack) {
        boolean b;
        try {
            b = size_Stack.getInt(stack) < 1;
        } catch (IllegalAccessException e) {
            b = true;
        }
        return stack == null || b;
    }

    @Override
    public void sendWindowProperty(IContainerListener listener, Container containerIn, int varToUpdate, int newValue) {
        try {
            sendProgressBarUpdate_IContainerListener.invoke(listener, containerIn, varToUpdate, newValue);
        } catch (ReflectiveOperationException ignore) {
        }
    }

    @Override
    public void onTake(Slot slot, EntityPlayer thePlayer, ItemStack stack) {
        try {
            onPickupFromSlot_Slot.invoke(slot, thePlayer, stack);
        } catch (ReflectiveOperationException ignore) {
        }
    }
}
