package com.yogpc.qp.container;

import java.util.HashSet;
import java.util.Set;

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
    public boolean canInteractWith(final EntityPlayer ep) {
        return this.tile.isUsableByPlayer(ep);
    }

    @Override
    public ItemStack transferStackInSlot(final EntityPlayer ep, final int i) {
        if (27 <= i && i < 45)
            return ItemStack.EMPTY;
        ItemStack src = ItemStack.EMPTY;
        final Slot slot = this.inventorySlots.get(i);
        if (slot != null && slot.getHasStack()) {
            final ItemStack remain = slot.getStack();
            src = remain.copy();
            if (i < 27) {
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
            slot.onTake(ep, remain);
        }
        return src;
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        listener.sendProgressBarUpdate(this, 0, tile.getRecipeIndex());
        listener.sendProgressBarUpdate(this, 1, (int) tile.getStoredEnergy());
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
            listener.sendProgressBarUpdate(this, 0, tile.getRecipeIndex());
            listener.sendProgressBarUpdate(this, 1, (int) tile.getStoredEnergy());
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
        int i = 0;

        if (stack.isStackable()) {
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
            i = 0;

            while (true) {
                if (i >= 27) {
                    break;
                }

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

                ++i;
            }
        }
        return flag;
    }

    private int field_94535_f = -1;
    /*
     * drag_state 0 start 1 content 2 end
     */
    private int drag_state;
    private final Set<Slot> dragged = new HashSet<>();

    /*
        @Override
        protected void resetDrag() {
            this.drag_state = 0;
            this.dragged.clear();
        }
    */
    public static boolean canDrag(final Slot slot, final ItemStack is, final boolean always_true) {
        boolean can = slot == null || !slot.getHasStack();
        if (slot != null && slot.getHasStack() && is != null && is.isItemEqual(slot.getStack())
                && ItemStack.areItemStackTagsEqual(slot.getStack(), is)) {
            final int i = always_true ? 0 : is.getCount();
            can |= slot.getStack().getCount() + i <= (slot instanceof SlotUnlimited ? slot.getSlotStackLimit() : is.getMaxStackSize());
        }
        return can;
    }

    /*
     * type 0 default 1 shift-click 2 hotbar 3 pickup 4 drop 5 dragged 6 double-click
     *//*
    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
        if (0 < slotId && slotId <= this.inventorySlots.size()) {
            final Slot c = this.inventorySlots.get(slotId);
            if (c instanceof SlotWorkbench) {
                if (clickTypeIn == ClickType.PICKUP) {
                    this.tile.currentRecipe = WorkbenchRecipes.getRecipeFromResult(c.getStack());
                    this.tile.markDirty();
                }
                return ItemStack.EMPTY;
            }
        }
        ItemStack ret = ItemStack.EMPTY;
        final InventoryPlayer player_inv = player.inventory;
        if (clickTypeIn == ClickType.SWAP) {
            final int prev_drag_state = this.drag_state;
            this.drag_state = func_94532_c(dragType);
            if ((prev_drag_state != 1 || this.drag_state != 2) && prev_drag_state != this.drag_state)
                resetDrag();
            else if (player_inv.getItemStack().isEmpty())
                resetDrag();
            else if (this.drag_state == 0) {
                this.field_94535_f = func_94529_b(dragType);
                if (func_94528_d(this.field_94535_f)) {
                    this.drag_state = 1;
                    this.dragged.clear();
                } else
                    resetDrag();
            } else if (this.drag_state == 1) {
                final Slot slot = this.inventorySlots.get(slotId);
                if (slot != null && canDrag(slot, player_inv.getItemStack(), true)
                        && slot.isItemValid(player_inv.getItemStack())
                        && player_inv.getItemStack().getCount() > this.dragged.size() && canDragIntoSlot(slot))
                    this.dragged.add(slot);
            } else if (this.drag_state == 2) {
                if (!this.dragged.isEmpty()) {
                    ItemStack player_stack = player_inv.getItemStack().copy();
                    int i1 = player_inv.getItemStack().getCount();
                    final Iterator<Slot> iterator = this.dragged.iterator();
                    while (iterator.hasNext()) {
                        final Slot slot = iterator.next();
                        if (slot != null && canDrag(slot, player_inv.getItemStack(), true)
                                && slot.isItemValid(player_inv.getItemStack())
                                && player_inv.getItemStack().getCount() >= this.dragged.size()
                                && canDragIntoSlot(slot)) {
                            final ItemStack slot_stack = player_stack.copy();
                            final int j1 = slot.getHasStack() ? slot.getStack().getCount() : 0;
                            func_94525_a(this.dragged, this.field_94535_f, slot_stack, j1);
                            if (!(slot instanceof SlotUnlimited)
                                    && slot_stack.getCount() > slot_stack.getMaxStackSize())
                                slot_stack.setCount(slot_stack.getMaxStackSize());
                            if (slot_stack.getCount() > slot.getSlotStackLimit())
                                slot_stack.setCount(slot.getSlotStackLimit());
                            i1 -= slot_stack.getCount() - j1;
                            slot.putStack(slot_stack);
                        }
                    }
                    player_stack.setCount(i1);
                    if (player_stack.getCount() <= 0)
                        player_stack = ItemStack.EMPTY;
                    player_inv.setItemStack(player_stack);
                }
                resetDrag();
            } else
                resetDrag();
        } else if (this.drag_state != 0)
            resetDrag();
        else if ((type == 0 || type == 1) && (dragType == 0 || dragType == 1)) {
            if (slotId == -999) {
                if (!player_inv.getItemStack().isEmpty() && slotId == -999) {
                    if (dragType == 0) {
                        player.dropPlayerItemWithRandomChoice(player_inv.getItemStack(), true);
                        player_inv.setItemStack(ItemStack.EMPTY);
                    }
                    if (dragType == 1) {
                        player.dropPlayerItemWithRandomChoice(player_inv.getItemStack().splitStack(1), true);
                        if (player_inv.getItemStack().getCount() == 0)
                            player_inv.setItemStack(ItemStack.EMPTY);
                    }
                }
            } else if (type == 1) {
                if (slotId < 0)
                    return ItemStack.EMPTY;
                final Slot slot = this.inventorySlots.get(slotId);
                if (slot != null && slot.canTakeStack(player)) {
                    final ItemStack itemstack3 = transferStackInSlot(player, slotId);
                    if (!itemstack3.isEmpty()) {
                        final Item item = itemstack3.getItem();
                        ret = itemstack3.copy();
                        if (!slot.getStack().isEmpty() && slot.getStack().getItem() == item)
                            retrySlotClick(slotId, dragType, true, player);
                    }
                }
            } else {
                if (slotId < 0)
                    return ItemStack.EMPTY;
                final Slot slot = this.inventorySlots.get(slotId);
                if (slot != null) {
                    ItemStack slot_stack = slot.getStack();
                    final ItemStack player_stack = player_inv.getItemStack();
                    if (!slot_stack.isEmpty())
                        ret = slot_stack.copy();
                    if (slot_stack.isEmpty()) {
                        if (!player_stack.isEmpty() && slot.isItemValid(player_stack)) {
                            int l1 = dragType == 0 ? player_stack.getCount() : 1;
                            if (l1 > slot.getSlotStackLimit())
                                l1 = slot.getSlotStackLimit();
                            if (player_stack.getCount() >= l1)
                                slot.putStack(player_stack.splitStack(l1));
                            if (player_stack.getCount() == 0)
                                player_inv.setItemStack(ItemStack.EMPTY);
                        }
                        //---------------------------------------------------------------------------------------
                    } else if (slot.canTakeStack(player))
                        if (player_stack == null) {
                            final int l1 = dragType == 0 ? slot_stack.getCount() : (slot_stack.getCount() + 1) / 2;
                            player_inv.setItemStack(slot.decrStackSize(l1));
                            if (slot_stack.getCount() == 0)
                                slot.putStack(ItemStack.EMPTY);
                            slot.onTake(player, player_inv.getItemStack());
                        } else if (slot.isItemValid(player_stack)) {
                            if (slot_stack.getItem() == player_stack.getItem()
                                    && slot_stack.getItemDamage() == player_stack.getItemDamage()
                                    && ItemStack.areItemStackTagsEqual(slot_stack, player_stack)) {
                                int player2slot = dragType == 0 ? player_stack.getCount() : 1;
                                if (player2slot > slot.getSlotStackLimit() - slot_stack.getCount())
                                    player2slot = slot.getSlotStackLimit() - slot_stack.getCount();
                                if (!(slot instanceof SlotUnlimited)
                                        && player2slot > player_stack.getMaxStackSize() - slot_stack.getCount())
                                    player2slot = player_stack.getMaxStackSize() - slot_stack.getCount();
                                player_stack.getCount() -= player2slot;
                                if (player_stack.getCount() == 0)
                                    player_inv.setItemStack(ItemStack.EMPTY);
                                slot_stack.getCount() += player2slot;
                            } else if (player_stack.getCount() <= slot.getSlotStackLimit()) {
                                slot.putStack(player_stack);
                                player_inv.setItemStack(slot_stack);
                            }
                        } else if (slot_stack.getItem() == player_stack.getItem()
                                && player_stack.getMaxStackSize() > 1
                                && (!slot_stack.getHasSubtypes() || slot_stack.getItemDamage() == player_stack
                                .getItemDamage()) && ItemStack.areItemStackTagsEqual(slot_stack, player_stack)) {
                            final int add2player = slot_stack.getCount();
                            if (add2player > 0
                                    && add2player + player_stack.getCount() <= player_stack.getMaxStackSize()) {
                                player_stack.getCount() += add2player;
                                slot_stack = slot.decrStackSize(add2player);
                                if (slot_stack.getCount() == 0)
                                    slot.putStack(ItemStack.EMPTY);
                                slot.onTake(player, player_inv.getItemStack());
                            }
                        }
                    slot.onSlotChanged();
                }
            }
        } else if (type == 2 && dragType >= 0 && dragType < 9) {
            final Slot slot = this.inventorySlots.get(slotId);
            if (slot.canTakeStack(player)) {
                final ItemStack player_stack = player_inv.getStackInSlot(dragType);
                boolean can2player =
                        player_stack == null || slot.inventory == player_inv && slot.isItemValid(player_stack);
                int first_empty = -1;
                if (!can2player) {
                    first_empty = player_inv.getFirstEmptyStack();
                    can2player |= first_empty > -1;
                }
                if (slot.getHasStack() && can2player) {// to player
                    final ItemStack slot_stack = slot.getStack();
                    player_inv.setInventorySlotContents(dragType, slot_stack.copy());
                    if ((slot.inventory != player_inv || !slot.isItemValid(player_stack))
                            && player_stack != null) {// prev2player
                        if (first_empty > -1) {
                            player_inv.addItemStackToInventory(player_stack);
                            slot.decrStackSize(slot_stack.getCount());
                            slot.putStack(ItemStack.EMPTY);
                            slot.onTake(player, slot_stack);
                        }
                    } else {// prev2slot
                        slot.decrStackSize(slot_stack.getCount());
                        slot.putStack(player_stack);
                        slot.onTake(player, slot_stack);
                    }
                } else if (!slot.getHasStack() && player_stack != null && slot.isItemValid(player_stack)) {
                    // player2slot
                    player_inv.setInventorySlotContents(dragType, null);
                    slot.putStack(player_stack);
                }
            }
        } else if (type == 3 && player.capabilities.isCreativeMode && player_inv.getItemStack() == null
                && slotId >= 0) {
            final Slot slot = this.inventorySlots.get(slotId);
            if (slot != null && slot.getHasStack()) {
                final ItemStack player_stack = slot.getStack().copy();
                player_stack.getCount() = player_stack.getMaxStackSize();
                player_inv.setItemStack(player_stack);
            }
        } else if (type == 4 && player_inv.getItemStack() == null && slotId >= 0) {
            final Slot slot = this.inventorySlots.get(slotId);
            if (slot != null && slot.getHasStack() && slot.canTakeStack(player)) {
                final ItemStack drop_stack = slot.decrStackSize(dragType == 0 ? 1 : slot.getStack().getCount());
                slot.onTake(player, drop_stack);
                player.dropPlayerItemWithRandomChoice(drop_stack, true);
            }
        } else if (type == 6 && slotId >= 0) {
            final Slot slot = this.inventorySlots.get(slotId);
            final ItemStack player_stack = player_inv.getItemStack();
            if (player_stack != null && (slot == null || !slot.getHasStack() || !slot.canTakeStack(player))) {
                final int i1 = dragType == 0 ? 0 : this.inventorySlots.size() - 1;
                final int l1 = dragType == 0 ? 1 : -1;
                for (int i2 = 0; i2 < 2; ++i2)
                    for (int j2 = i1; j2 >= 0 && j2 < this.inventorySlots.size()
                            && player_stack.getCount() < player_stack.getMaxStackSize(); j2 += l1) {
                        final Slot slot3 = this.inventorySlots.get(j2);
                        if (slot3.getHasStack() && canDrag(slot3, player_stack, true) && slot3.canTakeStack(player)
                                && func_94530_a(player_stack, slot3)
                                && (i2 != 0 || slot3.getStack().getCount() != slot3.getStack().getMaxStackSize())) {
                            final int k1 =
                                    Math.min(player_stack.getMaxStackSize() - player_stack.getCount(),
                                            slot3.getStack().getCount());
                            final ItemStack itemstack2 = slot3.decrStackSize(k1);
                            player_stack.getCount() += k1;
                            if (itemstack2.getCount() <= 0)
                                slot3.putStack(ItemStack.EMPTY);
                            slot3.onTake(player, itemstack2);
                        }
                    }
            }
            detectAndSendChanges();
        }
        return ret;
    }*/
/*
    @Override
    protected boolean mergeItemStack(final ItemStack is, final int from, final int to, final boolean invert) {
        boolean changed = false;
        int k = invert ? to - 1 : from;
        if (is.isStackable())
            while (is.stackSize > 0 && (!invert && k < to || invert && k >= from)) {
                final Slot slot = this.inventorySlots.get(k);
                final ItemStack slot_stack = slot.getStack();
                if (slot_stack != null && slot_stack.getItem() == is.getItem()
                        && (!is.getHasSubtypes() || is.getItemDamage() == slot_stack.getItemDamage())
                        && ItemStack.areItemStackTagsEqual(is, slot_stack)) {
                    int total = slot_stack.stackSize + is.stackSize;
                    if (!(slot instanceof SlotUnlimited) && total > slot_stack.getMaxStackSize())
                        total = slot_stack.getMaxStackSize();
                    if (total > slot.getSlotStackLimit())
                        total = slot.getSlotStackLimit();
                    if (total > slot_stack.stackSize) {
                        is.stackSize = slot_stack.stackSize + is.stackSize - total;
                        slot_stack.stackSize = total;
                        slot.onSlotChanged();
                        changed = true;
                    }
                }
                if (invert)
                    --k;
                else
                    ++k;
            }
        k = invert ? to - 1 : from;
        while (is.stackSize > 0 && (!invert && k < to || invert && k >= from)) {
            final Slot slot = this.inventorySlots.get(k);
            if (slot.getStack() == null) {
                final ItemStack slot_stack = is.copy();
                if (!(slot instanceof SlotUnlimited) && slot_stack.stackSize > slot_stack.getMaxStackSize())
                    slot_stack.stackSize = slot_stack.getMaxStackSize();
                if (slot_stack.stackSize > slot.getSlotStackLimit())
                    slot_stack.stackSize = slot.getSlotStackLimit();
                slot.putStack(slot_stack);
                slot.onSlotChanged();
                is.stackSize -= slot_stack.stackSize;
                changed = true;
            }
            if (invert)
                --k;
            else
                ++k;
        }
        return changed;
    }

    */
}
