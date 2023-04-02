package com.yogpc.qp.machines.workbench;

import java.util.Objects;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.misc.SlotContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerWorkbench extends AbstractContainerMenu {

    final TileWorkbench tile;
    private static final int sourceSlot = 27;
    private static final int recipeSlot = 18;
    private static final int playerSlot = 36;
    final DataSlot progress = this.addDataSlot(DataSlot.standalone());
    final DataSlot isWorking = this.addDataSlot(DataSlot.standalone());
    final DataSlot workContinue = this.addDataSlot(DataSlot.standalone());
    final DataSlot recipeIndex = this.addDataSlot(DataSlot.standalone());

    public ContainerWorkbench(int id, Player player, BlockPos pos) {
        super(Holder.WORKBENCH_MENU_TYPE, id);
        this.tile = Objects.requireNonNull(((TileWorkbench) player.level.getBlockEntity(pos)));
        int row;
        int col;

        //0-26
        for (row = 0; row < 3; ++row)
            for (col = 0; col < 9; ++col)
                addSlot(new SlotContainer(tile, col + row * 9, 8 + col * 18, 18 + row * 18));

        //27-44
        for (row = 0; row < 2; ++row)
            for (col = 0; col < 9; ++col)
                addSlot(new SlotContainer(tile, col + row * 9 + sourceSlot, 8 + col * 18, 90 + row * 18));

        //45-62
        for (row = 0; row < 3; ++row)
            for (col = 0; col < 9; ++col)
                addSlot(new Slot(player.getInventory(), col + row * 9 + 9, 8 + col * 18, 140 + row * 18));

        //63-71
        for (col = 0; col < 9; ++col)
            addSlot(new Slot(player.getInventory(), col, 8 + col * 18, 198));

        if (!player.level.isClientSide) {
            setTrackValues();
            tile.startOpen(player);
        }
    }

    private void setTrackValues() {
        progress.set(this.tile.getProgressScaled(160));
        isWorking.set(this.tile.getRecipe().hasContent() ? 1 : 0);
        workContinue.set(this.tile.workContinue ? 1 : 0);
        recipeIndex.set(this.tile.recipesList.indexOf(this.tile.getRecipe()));
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return this.tile.stillValid(playerIn);
    }

    /**
     * @param index The index of clicked slot, the source.
     */
    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        if (sourceSlot <= index && index < sourceSlot + recipeSlot)
            return ItemStack.EMPTY;
        ItemStack src = ItemStack.EMPTY;
        final Slot slot = this.getSlot(index);
        if (slot.hasItem()) {
            final ItemStack remain = slot.getItem();
            src = remain.copy();
            if (index < sourceSlot) {
                //To inventory
                if (src.isStackable()) {
                    if (!moveItemStackTo(remain, sourceSlot + recipeSlot, sourceSlot + recipeSlot + playerSlot, true))
                        return ItemStack.EMPTY;
                } else {
                    for (int i = sourceSlot + recipeSlot + playerSlot - 1; i >= sourceSlot + recipeSlot && !remain.isEmpty(); i--) {
                        Slot destinationSlot = getSlot(i);

                        if (!destinationSlot.hasItem()) {
                            //Just move
                            int maxSize = Math.min(slot.getMaxStackSize(), remain.getMaxStackSize());
                            destinationSlot.set(remain.split(maxSize));
                        } else {
                            ItemStack dest = destinationSlot.getItem();
                            if (ItemStack.isSameItemSameTags(dest, remain)) {
                                int newSize = dest.getCount() + remain.getCount();
                                int maxSize = Math.min(slot.getMaxStackSize(), remain.getMaxStackSize());

                                if (newSize <= maxSize) {
                                    remain.setCount(0);
                                    dest.setCount(newSize);
                                    slot.setChanged();
                                } else if (dest.getCount() < maxSize) {
                                    remain.shrink(maxSize - dest.getCount());
                                    dest.setCount(maxSize);
                                    slot.setChanged();
                                }
                            }
                        }
                    }
                    if (!remain.isEmpty()) {
                        slot.setChanged();
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!n_moveItemStackTo(remain)) {
                //To workbench
                return ItemStack.EMPTY;
            }
            if (remain.isEmpty())
                slot.set(ItemStack.EMPTY);
            else
                slot.setChanged();
            if (remain.getCount() == src.getCount())
                return ItemStack.EMPTY;
            slot.onTake(playerIn, remain);
        }
        return src;
    }

    @Override
    public void broadcastChanges() {
        if (this.tile != null) {
            setTrackValues();
        }
        super.broadcastChanges();
    }

    /**
     * @param slotId      slot id
     * @param dragType    0 = left click, 1 right click, 2 middle click.
     * @param clickTypeIn NORMAL->PICKUP, Shift click->QUICK_MOVE, Middle click->CLONE
     * @param player      the player
     */
    @Override
    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
        if (sourceSlot <= slotId && slotId < sourceSlot + recipeSlot && clickTypeIn == ClickType.PICKUP) {
            int index = slotId - sourceSlot;
            if (index < tile.recipesList.size()) {
                if (dragType == 0) {
                    var newRecipeId = tile.recipesList.get(index).getId();
                    if (newRecipeId.equals(tile.getRecipe().getId())) {
                        tile.workContinue = !tile.workContinue;
                    } else {
                        tile.setCurrentRecipe(newRecipeId);
                    }
                } else if (dragType == 1) {
                    tile.setCurrentRecipe(WorkbenchRecipe.dummyRecipe().getId());
                }
            }
        } else if (0 <= slotId && slotId < sourceSlot && clickTypeIn == ClickType.PICKUP) {
            Slot slot = this.getSlot(slotId);

            ItemStack slotStack = slot.getItem();
            ItemStack carrying = getCarried();

            if (slotStack.isEmpty()) {
                // push TO workbench.
                if (!carrying.isEmpty() && slot.mayPlace(carrying)) {
                    int l2 = dragType == 0 ? carrying.getCount() : 1;

                    if (l2 > slot.getMaxStackSize(carrying)) {
                        l2 = slot.getMaxStackSize(carrying);
                    }
                    slot.set(carrying.split(l2));
                }
            } else {
                if (carrying.isEmpty()) {
                    // take ItemStack FROM workbench
                    int k2;
                    if (dragType == 0) {
                        k2 = Math.min(slotStack.getCount(), slotStack.getMaxStackSize());
                    } else {
                        k2 = Math.min((slotStack.getCount() + 1) / 2, slotStack.getMaxStackSize());
                    }
                    setCarried(slot.remove(k2));

                    if (slotStack.isEmpty()) {
                        slot.set(ItemStack.EMPTY);
                    }

                    slot.onTake(player, getCarried());
                } else {
                    // push TO workbench.
                    if (ItemStack.isSameItemSameTags(slotStack, carrying)) {
                        int j2 = dragType == 0 ? carrying.getCount() : 1;

                        carrying.shrink(j2);
                        slotStack.grow(j2);
                    }
                }
            }
            slot.setChanged();
        } else if (clickTypeIn != ClickType.SWAP) {
            // Fix https://github.com/Kotori316/QuarryPlus/issues/253
            // Do nothing when number key or F key is pressed to avoid over-stacked items
            super.clicked(slotId, dragType, clickTypeIn, player);
        }
    }

    protected boolean n_moveItemStackTo(ItemStack stack) {
        boolean flag = false;

        for (int i = 0; i < sourceSlot && !stack.isEmpty(); i++) {
            Slot slot = getSlot(i);
            ItemStack itemstack = slot.getItem();

            if (!itemstack.isEmpty() && ItemStack.isSameItemSameTags(stack, itemstack)) {
                int j = itemstack.getCount() + stack.getCount();
                int maxSize = slot.getMaxStackSize();// Ignore limit of stack. Math.min(slot.getMaxStackSize(), stack.getMaxStackSize());

                if (j <= maxSize) {
                    stack.setCount(0);
                    itemstack.setCount(j);
                    slot.setChanged();
                    flag = true;
                } else if (itemstack.getCount() < maxSize) {
                    //come?
                    if (QuarryPlus.config.debug())
                        QuarryPlus.LOGGER.info("ContainerWorkbench#mergeItemStack itemStack.getCount() < maxSize");
                    stack.shrink(maxSize - itemstack.getCount());
                    itemstack.setCount(maxSize);
                    slot.setChanged();
                    flag = true;
                }
            }
        }
        if (!stack.isEmpty()) {
            for (int i = 0; i < sourceSlot; i++) {
                Slot slot1 = getSlot(i);
                ItemStack itemStack1 = slot1.getItem();

                if (itemStack1.isEmpty() && slot1.mayPlace(stack)) {
                    //NEVER
                    /* if (stack.getCount() > slot1.getMaxStackSize()) {
                        slot1.set(stack.split(slot1.getMaxStackSize()));
                    } else */
                    slot1.set(stack.split(stack.getCount()));

                    slot1.setChanged();
                    flag = true;
                    break;
                }
            }
        }
        return flag;
    }

    @Override
    public void removed(Player playerIn) {
        super.removed(playerIn);
        this.tile.stopOpen(playerIn);
    }

}
