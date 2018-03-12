package com.yogpc.qp.container;

import com.yogpc.qp.Config;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.tile.TileWorkbench;
import com.yogpc.qp.version.VersionUtil;
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

import static com.yogpc.qp.version.VersionUtil.empty;
import static com.yogpc.qp.version.VersionUtil.getCount;
import static com.yogpc.qp.version.VersionUtil.isEmpty;
import static com.yogpc.qp.version.VersionUtil.nonEmpty;

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
            return empty();
        ItemStack src = empty();
        final Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            final ItemStack remain = slot.getStack();
            src = remain.copy();
            if (index < 27) {
                //To inventory
                if (!mergeItemStack(remain, 45, 72, true))
                    return empty();
            } else if (!n_mergeItemStack(remain))
                //To workbench
                return empty();
            if (isEmpty(remain))
                slot.putStack(empty());
            else
                slot.onSlotChanged();
            if (getCount(remain) == getCount(src))
                return empty();
            VersionUtil.onTake(slot, playerIn, remain);
        }
        return src;
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        listener.sendAllWindowProperties(this, tile);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data) {
        super.updateProgressBar(id, data);
        tile.setField(id, data);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        listeners.forEach(listener -> listener.sendAllWindowProperties(this, tile));
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
                if (dragType == 0) {
                    if (index == tile.getRecipeIndex()) {
                        tile.workcontinue = !tile.workcontinue;
                    } else {
                        tile.setCurrentRecipe(index);
                    }
                } else if (dragType == 1) {
                    tile.setCurrentRecipe(-1);
                }
            }
            return empty();
        } else if (0 <= slotId && slotId < 27 && clickTypeIn == ClickType.PICKUP) {

            InventoryPlayer inventoryplayer = player.inventory;
            ItemStack itemstack = empty();

            Slot slot = this.inventorySlots.get(slotId);

            if (slot != null) {
                ItemStack slotStack = slot.getStack();
                ItemStack playerStack = inventoryplayer.getItemStack();

                if (nonEmpty(slotStack)) {
                    itemstack = slotStack.copy();
                }

                if (isEmpty(slotStack)) {
                    //put TO workbench.
                    if (nonEmpty(playerStack) && slot.isItemValid(playerStack)) {
                        int l2 = dragType == 0 ? getCount(playerStack) : 1;

                        if (l2 > slot.getItemStackLimit(playerStack)) {
                            l2 = slot.getItemStackLimit(playerStack);
                        }
                        slot.putStack(playerStack.splitStack(l2));
                        if (VersionUtil.isEmpty(playerStack)) {
                            inventoryplayer.setItemStack(VersionUtil.empty());
                        }
                    }
                } else {
                    if (isEmpty(playerStack)) {
                        //take itemstack FROM workbench
                        if (isEmpty(slotStack)) {
                            slot.putStack(empty());
                            inventoryplayer.setItemStack(empty());
                        } else {
                            int k2;
                            if (dragType == 0) {
                                k2 = Math.min(getCount(slotStack), slotStack.getMaxStackSize());
                            } else {
                                k2 = Math.min((getCount(slotStack) + 1) / 2, slotStack.getMaxStackSize());
                            }
                            inventoryplayer.setItemStack(slot.decrStackSize(k2));

                            if (isEmpty(slotStack)) {
                                slot.putStack(empty());
                            }

                            VersionUtil.onTake(slot, player, inventoryplayer.getItemStack());
                        }
                    } else {
                        //put TO workbench.
                        if (slotStack.getItem() == playerStack.getItem() && slotStack.getMetadata() == playerStack.getMetadata()
                            && ItemStack.areItemStackTagsEqual(slotStack, playerStack)) {
                            int j2 = dragType == 0 ? getCount(playerStack) : 1;

                            VersionUtil.shrink(playerStack, j2);
                            VersionUtil.grow(slotStack, j2);
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
            while (nonEmpty(stack)) {
                if (i >= 27) {
                    break;
                }
                Slot slot = this.inventorySlots.get(i);
                ItemStack itemstack = slot.getStack();

                if (nonEmpty(itemstack) && itemstack.getItem() == stack.getItem() &&
                    (!stack.getHasSubtypes() || stack.getMetadata() == itemstack.getMetadata()) && ItemStack.areItemStackTagsEqual(stack, itemstack)) {
                    int j = getCount(itemstack) + getCount(stack);
                    int maxSize = slot.getSlotStackLimit();// ignore limit of stack. Math.min(slot.getSlotStackLimit(), stack.getMaxStackSize());

                    if (j <= maxSize) {
                        VersionUtil.setCount(stack, 0);
                        VersionUtil.setCount(itemstack, j);
                        slot.onSlotChanged();
                        flag = true;
                    } else if (getCount(itemstack) < maxSize) {
                        //come?
                        if (Config.content().debug())
                            QuarryPlus.LOGGER.info("ContainerWorkbench#mergeItemStack itemstack.getCount() < maxSize");
                        VersionUtil.shrink(stack, maxSize - getCount(itemstack));
                        VersionUtil.setCount(itemstack, maxSize);
                        slot.onSlotChanged();
                        flag = true;
                    }
                }
                ++i;
            }
        }
        if (nonEmpty(stack)) {
            for (int i = 0; i < 27; i++) {
                Slot slot1 = this.inventorySlots.get(i);
                ItemStack itemstack1 = slot1.getStack();

                if (isEmpty(itemstack1) && slot1.isItemValid(stack)) {
                    //NEVER
                    /* if (stack.getCount() > slot1.getSlotStackLimit()) {
                        slot1.putStack(stack.splitStack(slot1.getSlotStackLimit()));
                    } else */
                    slot1.putStack(stack.splitStack(getCount(stack)));

                    slot1.onSlotChanged();
                    flag = true;
                    break;
                }
            }
        }
        return flag;
    }
}
