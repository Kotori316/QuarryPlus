package com.yogpc.qp.container;

import com.yogpc.qp.Config;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.tile.TileWorkbench;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerWorkbench extends Container {

    private final TileWorkbench tile;

    public ContainerWorkbench(final IInventory pi, final TileWorkbench tw) {
        this.tile = tw;
        int row;
        int col;

        //0-26
        for (row = 0; row < 3; ++row)
            for (col = 0; col < 9; ++col)
                addSlotToContainer(new SlotUnlimited(tw, col + row * 9, 8 + col * 18, 18 + row * 18));

        //27-44
        for (row = 0; row < 2; ++row)
            for (col = 0; col < 9; ++col)
                addSlotToContainer(new SlotWorkbench(tw, col + row * 9 + 27, 8 + col * 18, 90 + row * 18));

        //45-62
        for (row = 0; row < 3; ++row)
            for (col = 0; col < 9; ++col)
                addSlotToContainer(new Slot(pi, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));

        //63-71
        for (col = 0; col < 9; ++col)
            addSlotToContainer(new Slot(pi, col, 8 + col * 18, 198));
    }

    @Override
    public boolean canInteractWith(final EntityPlayer playerIn) {
        return this.tile.isUsableByPlayer(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(final EntityPlayer playerIn, final int index) {
        if (27 <= index && index < 45)
            return ItemStack.EMPTY;
        ItemStack src = ItemStack.EMPTY;
        final Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            final ItemStack remain = slot.getStack();
            src = remain.copy();
            if (index < 27) {
                //To inventory
                if (!mergeItemStack(remain, 45, 72, true))
                    return ItemStack.EMPTY;
            } else if (!n_mergeItemStack(remain))
                //To workbench
                return ItemStack.EMPTY;
            if (remain.isEmpty())
                slot.putStack(ItemStack.EMPTY);
            else
                slot.onSlotChanged();
            if (remain.getCount() == src.getCount())
                return ItemStack.EMPTY;
            slot.onTake(playerIn, remain);
        }
        return src;
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        listener.sendWindowProperty(this, 0, tile.getRecipeIndex());
        listener.sendWindowProperty(this, 1, (int) tile.getStoredEnergy());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data) {
        super.updateProgressBar(id, data);
        switch (id) {
            case 0:
                tile.setCurrentRecipe(data);
                break;
            case 1:
                tile.setStoredEnergy(data);
                break;
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (IContainerListener listener : listeners) {
            listener.sendWindowProperty(this, 0, tile.getRecipeIndex());
            listener.sendWindowProperty(this, 1, (int) tile.getStoredEnergy());
        }
    }

    /**
     * @param slotId      slot id
     * @param dragType    0 = left click, 1 right click, 2 middle click.
     * @param clickTypeIn NORMAL->PICKUP, Shift click->QUICK_MOVE, Middle click->CLONE
     * @param player      the player
     * @return ???
     */
    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
        if (27 <= slotId && slotId < 45 && clickTypeIn == ClickType.PICKUP) {
            int index = slotId - 27;
            if (index < tile.recipesList.size()) {
                tile.setCurrentRecipe(index);
            }
            return ItemStack.EMPTY;
        } else if (0 <= slotId && slotId < 27 && clickTypeIn == ClickType.PICKUP) {

            InventoryPlayer inventoryplayer = player.inventory;
            ItemStack itemstack = ItemStack.EMPTY;

            Slot slot = this.inventorySlots.get(slotId);

            if (slot != null) {
                ItemStack slotStack = slot.getStack();
                ItemStack playerStack = inventoryplayer.getItemStack();

                if (!slotStack.isEmpty()) {
                    itemstack = slotStack.copy();
                }

                if (slotStack.isEmpty()) {
                    //put TO workbench.
                    if (!playerStack.isEmpty() && slot.isItemValid(playerStack)) {
                        int l2 = dragType == 0 ? playerStack.getCount() : 1;

                        if (l2 > slot.getItemStackLimit(playerStack)) {
                            l2 = slot.getItemStackLimit(playerStack);
                        }
                        slot.putStack(playerStack.splitStack(l2));
                    }
                } else {
                    if (playerStack.isEmpty()) {
                        //take itemstack FROM workbench
                        if (slotStack.isEmpty()) {
                            slot.putStack(ItemStack.EMPTY);
                            inventoryplayer.setItemStack(ItemStack.EMPTY);
                        } else {
                            int k2;
                            if (dragType == 0) {
                                k2 = Math.min(slotStack.getCount(), slotStack.getMaxStackSize());
                            } else {
                                k2 = Math.min((slotStack.getCount() + 1) / 2, slotStack.getMaxStackSize());
                            }
                            inventoryplayer.setItemStack(slot.decrStackSize(k2));

                            if (slotStack.isEmpty()) {
                                slot.putStack(ItemStack.EMPTY);
                            }

                            slot.onTake(player, inventoryplayer.getItemStack());
                        }
                    } else {
                        //put TO workbench.
                        if (slotStack.getItem() == playerStack.getItem() && slotStack.getMetadata() == playerStack.getMetadata()
                                && ItemStack.areItemStackTagsEqual(slotStack, playerStack)) {
                            int j2 = dragType == 0 ? playerStack.getCount() : 1;

                            playerStack.shrink(j2);
                            slotStack.grow(j2);
                        }
                        //Swiching items. not need!
                        /*else if (playerStack.getCount() <= slot.getItemStackLimit(playerStack)) {

                            slot.putStack(playerStack);
                            inventoryplayer.setItemStack(slotStack);
                        }*/
                    }
                }
                slot.onSlotChanged();
            }
            return itemstack;
        } else
            return super.slotClick(slotId, dragType, clickTypeIn, player);
    }

    protected boolean n_mergeItemStack(ItemStack stack) {
        boolean flag = false;

        if (stack.isStackable()) {
            int i = 0;
            while (!stack.isEmpty()) {
                if (i >= 27) {
                    break;
                }
                Slot slot = this.inventorySlots.get(i);
                ItemStack itemstack = slot.getStack();

                if (!itemstack.isEmpty() && itemstack.getItem() == stack.getItem() &&
                        (!stack.getHasSubtypes() || stack.getMetadata() == itemstack.getMetadata()) && ItemStack.areItemStackTagsEqual(stack, itemstack)) {
                    int j = itemstack.getCount() + stack.getCount();
                    int maxSize = slot.getSlotStackLimit();// ignore limit of stack. Math.min(slot.getSlotStackLimit(), stack.getMaxStackSize());

                    if (j <= maxSize) {
                        stack.setCount(0);
                        itemstack.setCount(j);
                        slot.onSlotChanged();
                        flag = true;
                    } else if (itemstack.getCount() < maxSize) {
                        //come?
                        if (Config.content().debug())
                            QuarryPlus.LOGGER.info("ContainerWorkbench#mergeItemStack itemstack.getCount() < maxSize");
                        stack.shrink(maxSize - itemstack.getCount());
                        itemstack.setCount(maxSize);
                        slot.onSlotChanged();
                        flag = true;
                    }
                }
                ++i;
            }
        }
        if (!stack.isEmpty()) {
            for (int i = 0; i < 27; i++) {
                Slot slot1 = this.inventorySlots.get(i);
                ItemStack itemstack1 = slot1.getStack();

                if (itemstack1.isEmpty() && slot1.isItemValid(stack)) {
                    //NEVER
                    /* if (stack.getCount() > slot1.getSlotStackLimit()) {
                        slot1.putStack(stack.splitStack(slot1.getSlotStackLimit()));
                    } else */
                    slot1.putStack(stack.splitStack(stack.getCount()));

                    slot1.onSlotChanged();
                    flag = true;
                    break;
                }
            }
        }
        return flag;
    }
}
