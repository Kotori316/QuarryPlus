package com.yogpc.qp.machines.workbench;

import com.yogpc.qp.Config;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.base.SlotUnlimited;
import com.yogpc.qp.machines.base.SlotWorkbench;
import com.yogpc.qp.utils.Holder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.math.BlockPos;

public class ContainerWorkbench extends Container {

    final TileWorkbench tile;
    private static final int sourceSlot = 27;
    private static final int recipeSlot = 18;
    private static final int playerSlot = 36;
    final IntReferenceHolder progress = this.trackInt(IntReferenceHolder.single());
    final IntReferenceHolder isWorking = this.trackInt(IntReferenceHolder.single());
    final IntReferenceHolder workContinue = this.trackInt(IntReferenceHolder.single());
    final IntReferenceHolder recipeIndex = this.trackInt(IntReferenceHolder.single());

    public ContainerWorkbench(int id, final PlayerEntity player, BlockPos pos) {
        super(Holder.workbenchContainerType(), id);
        this.tile = ((TileWorkbench) player.getEntityWorld().getTileEntity(pos));
        int row;
        int col;

        //0-26
        for (row = 0; row < 3; ++row)
            for (col = 0; col < 9; ++col)
                addSlot(new SlotUnlimited(tile, col + row * 9, 8 + col * 18, 18 + row * 18));

        //27-44
        for (row = 0; row < 2; ++row)
            for (col = 0; col < 9; ++col)
                addSlot(new SlotWorkbench(tile, col + row * 9 + sourceSlot, 8 + col * 18, 90 + row * 18));

        //45-62
        for (row = 0; row < 3; ++row)
            for (col = 0; col < 9; ++col)
                addSlot(new Slot(player.inventory, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));

        //63-71
        for (col = 0; col < 9; ++col)
            addSlot(new Slot(player.inventory, col, 8 + col * 18, 198));

        if (!player.world.isRemote && this.tile != null) {
            setTrackValues();
        }
    }

    private void setTrackValues() {
        progress.set(this.tile.getProgressScaled(160));
        isWorking.set(this.tile.isWorking() ? 1 : 0);
        workContinue.set(this.tile.workContinue ? 1 : 0);
        recipeIndex.set(this.tile.getRecipeIndex());
    }

    @Override
    public boolean canInteractWith(final PlayerEntity playerIn) {
        return this.tile.isUsableByPlayer(playerIn);
    }

    /**
     * @param index The index of clicked slot, the source.
     */
    @Override
    public ItemStack transferStackInSlot(final PlayerEntity playerIn, final int index) {
        if (sourceSlot <= index && index < sourceSlot + recipeSlot)
            return ItemStack.EMPTY;
        ItemStack src = ItemStack.EMPTY;
        final Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            final ItemStack remain = slot.getStack();
            src = remain.copy();
            if (index < sourceSlot) {
                //To inventory
                if (src.isStackable()) {
                    if (!mergeItemStack(remain, sourceSlot + recipeSlot, sourceSlot + recipeSlot + playerSlot, true))
                        return ItemStack.EMPTY;
                } else {
                    for (int i = sourceSlot + recipeSlot + playerSlot - 1; i >= sourceSlot + recipeSlot && !remain.isEmpty(); i--) {
                        Slot destinationSlot = inventorySlots.get(i);

                        if (!destinationSlot.getHasStack()) {
                            //Just move
                            int maxSize = Math.min(slot.getSlotStackLimit(), remain.getMaxStackSize());
                            destinationSlot.putStack(remain.split(maxSize));
                        } else {
                            ItemStack dest = destinationSlot.getStack();
                            if (areStack_Able(dest, remain)) {
                                int newSize = dest.getCount() + remain.getCount();
                                int maxSize = Math.min(slot.getSlotStackLimit(), remain.getMaxStackSize());

                                if (newSize <= maxSize) {
                                    remain.setCount(0);
                                    dest.setCount(newSize);
                                    slot.onSlotChanged();
                                } else if (dest.getCount() < maxSize) {
                                    remain.shrink(maxSize - dest.getCount());
                                    dest.setCount(maxSize);
                                    slot.onSlotChanged();
                                }
                            }
                        }
                    }
                    if (!remain.isEmpty())
                        return ItemStack.EMPTY;
                }
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
    public void detectAndSendChanges() {
        if (this.tile != null) {
            setTrackValues();
        }
        super.detectAndSendChanges();
    }

    /**
     * @param slotId      slot id
     * @param dragType    0 = left click, 1 right click, 2 middle click.
     * @param clickTypeIn NORMAL->PICKUP, Shift click->QUICK_MOVE, Middle click->CLONE
     * @param player      the player
     * @return ???
     */
    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
        if (sourceSlot <= slotId && slotId < sourceSlot + recipeSlot && clickTypeIn == ClickType.PICKUP) {
            int index = slotId - sourceSlot;
            if (index < tile.recipesList.size()) {
                if (dragType == 0) {
                    if (index == tile.getRecipeIndex()) {
                        tile.workContinue = !tile.workContinue;
                    } else {
                        tile.setCurrentRecipeIndex(index);
                    }
                } else if (dragType == 1) {
                    tile.setCurrentRecipeIndex(-1);
                }
            }
            return ItemStack.EMPTY;
        } else if (0 <= slotId && slotId < sourceSlot && clickTypeIn == ClickType.PICKUP) {

            PlayerInventory playerInventory = player.inventory;
            ItemStack itemstack = ItemStack.EMPTY;

            Slot slot = this.inventorySlots.get(slotId);

            if (slot != null) {
                ItemStack slotStack = slot.getStack();
                ItemStack playerStack = playerInventory.getItemStack();

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
                        slot.putStack(playerStack.split(l2));
                    }
                } else {
                    if (playerStack.isEmpty()) {
                        //take ItemStack FROM workbench
                        int k2;
                        if (dragType == 0) {
                            k2 = Math.min(slotStack.getCount(), slotStack.getMaxStackSize());
                        } else {
                            k2 = Math.min((slotStack.getCount() + 1) / 2, slotStack.getMaxStackSize());
                        }
                        playerInventory.setItemStack(slot.decrStackSize(k2));

                        if (slotStack.isEmpty()) {
                            slot.putStack(ItemStack.EMPTY);
                        }

                        slot.onTake(player, playerInventory.getItemStack());
                    } else {
                        //put TO workbench.
                        if (areStack_Able(slotStack, playerStack)) {
                            int j2 = dragType == 0 ? playerStack.getCount() : 1;

                            playerStack.shrink(j2);
                            slotStack.grow(j2);
                        }
                        //Switching items. not need!
                        /*else if (playerStack.getCount() <= slot.getItemStackLimit(playerStack)) {

                            slot.putStack(playerStack);
                            playerInventory.setItemStack(slotStack);
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

        for (int i = 0; i < sourceSlot && !stack.isEmpty(); i++) {
            Slot slot = this.inventorySlots.get(i);
            ItemStack itemstack = slot.getStack();

            if (!itemstack.isEmpty() && areStack_Able(stack, itemstack)) {
                int j = itemstack.getCount() + stack.getCount();
                int maxSize = slot.getSlotStackLimit();// ignore limit of stack. Math.min(slot.getSlotStackLimit(), stack.getMaxStackSize());

                if (j <= maxSize) {
                    stack.setCount(0);
                    itemstack.setCount(j);
                    slot.onSlotChanged();
                    flag = true;
                } else if (itemstack.getCount() < maxSize) {
                    //come?
                    if (Config.common().debug())
                        QuarryPlus.LOGGER.info("ContainerWorkbench#mergeItemStack itemStack.getCount() < maxSize");
                    stack.shrink(maxSize - itemstack.getCount());
                    itemstack.setCount(maxSize);
                    slot.onSlotChanged();
                    flag = true;
                }
            }
        }
        if (!stack.isEmpty()) {
            for (int i = 0; i < sourceSlot; i++) {
                Slot slot1 = this.inventorySlots.get(i);
                ItemStack itemStack1 = slot1.getStack();

                if (itemStack1.isEmpty() && slot1.isItemValid(stack)) {
                    //NEVER
                    /* if (stack.getCount() > slot1.getSlotStackLimit()) {
                        slot1.putStack(stack.split(slot1.getSlotStackLimit()));
                    } else */
                    slot1.putStack(stack.split(stack.getCount()));

                    slot1.onSlotChanged();
                    flag = true;
                    break;
                }
            }
        }
        return flag;
    }

    private static boolean areStack_Able(ItemStack stack1, ItemStack stack2) {
        return ItemStack.areItemsEqual(stack1, stack2) && ItemStack.areItemStackTagsEqual(stack1, stack2);
    }
}
