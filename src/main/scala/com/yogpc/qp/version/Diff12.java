package com.yogpc.qp.version;

import java.lang.reflect.Method;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class Diff12 implements VersionDiff {

    private final ItemStack empty_Stack;
    private final Method isempty_Stack;
    private final Method onTake_Slot;
    private final Method sendWindowProperty_IContainerListener;

    @SuppressWarnings("JavaReflectionMemberAccess")
    public Diff12() throws ReflectiveOperationException {
        empty_Stack = (ItemStack) ItemStack.class.getField("EMPTY").get(null);
        isempty_Stack = ItemStack.class.getMethod("isEmpty");
        onTake_Slot = Slot.class.getMethod("onTake", EntityPlayer.class, ItemStack.class);
        sendWindowProperty_IContainerListener = IContainerListener.class.getMethod("sendWindowProperty", Container.class, int.class, int.class);
    }

    @Override
    public ItemStack empty() {
        return empty_Stack;
    }

    @Override
    public boolean isEmpty(ItemStack stack) {
        try {
            return ((Boolean) isempty_Stack.invoke(stack));
        } catch (ReflectiveOperationException ignore) {
            return true;
        }
    }

    @Override
    public void sendWindowProperty(IContainerListener listener, Container containerIn, int varToUpdate, int newValue) {
        try {
            sendWindowProperty_IContainerListener.invoke(listener, containerIn, varToUpdate, newValue);
        } catch (ReflectiveOperationException ignore) {
        }
    }

    @Override
    public void onTake(Slot slot, EntityPlayer thePlayer, ItemStack stack) {
        try {
            onTake_Slot.invoke(slot, thePlayer, stack);
        } catch (ReflectiveOperationException ignore) {
        }
    }
}
