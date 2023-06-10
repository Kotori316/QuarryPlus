package com.yogpc.qp.machines.module;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.misc.SlotContainer;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public final class FilterModuleMenu extends AbstractContainerMenu {
    public static final String GUI_ID = QuarryPlus.modID + ":gui_" + FilterModuleItem.NAME;
    private final ModuleContainer container;
    private final int allSlots;

    public FilterModuleMenu(int pContainerId, Player player, ItemStack filterModuleItem) {
        super(Holder.FILTER_MODULE_MENU_TYPE, pContainerId);
        this.container = new ModuleContainer(filterModuleItem);
        final int rows = 2;
        this.allSlots = rows * 9;
        int i = (rows - 4) * 18;

        final int oneBox = 18;
        // Filter item inventory
        for (int j = 0; j < rows; ++j) {
            for (int k = 0; k < 9; ++k) {
                this.addSlot(new SlotContainer(container, k + j * 9, 8 + k * oneBox, 18 + j * oneBox));
            }
        }

        // Player
        for (int l = 0; l < 3; ++l) {
            for (int j1 = 0; j1 < 9; ++j1) {
                this.addSlot(new Slot(player.getInventory(), j1 + l * 9 + 9, 8 + j1 * oneBox, 103 + l * oneBox + i));
            }
        }
        for (int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new NotPickUpSlot(player.getInventory(), i1, 8 + i1 * oneBox, 161 + i));
        }

    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        Slot slot = this.slots.get(pIndex);
        if (slot.hasItem()) {
            ItemStack slotItem = slot.getItem();
            ItemStack copy = slotItem.copy();
            if (pIndex < this.allSlots) {
                if (!this.moveItemStackTo(slotItem, this.allSlots, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(slotItem, 0, this.allSlots, false)) {
                return ItemStack.EMPTY;
            }

            if (slotItem.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            return copy;
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }

    @Override
    public void removed(Player pPlayer) {
        super.removed(pPlayer);
        this.container.stopOpen(pPlayer);
    }

    private static class ModuleContainer extends SimpleContainer {
        private final ItemStack filterModuleItem;

        public ModuleContainer(ItemStack filterModuleItem) {
            super(18);
            this.filterModuleItem = filterModuleItem;
            FilterModule.getFromTag(Optional.ofNullable(filterModuleItem.getTag())
                            .map(t -> t.getList(FilterModuleItem.KEY_ITEMS, Tag.TAG_COMPOUND)).orElse(null))
                    .stream()
                    .map(itemKey -> itemKey.toStack(1))
                    .forEach(this::addItem);
        }

        @Override
        public final int getMaxStackSize() {
            return 1;
        }

        @Override
        public void stopOpen(Player pPlayer) {
            super.stopOpen(pPlayer);
            if (this.isEmpty()) {
                filterModuleItem.removeTagKey(FilterModuleItem.KEY_ITEMS);
            } else {
                filterModuleItem.addTagElement(FilterModuleItem.KEY_ITEMS, FilterModule.getFromItems(this.removeAllItems()));
            }
        }

        @Override
        public boolean canPlaceItem(int pIndex, ItemStack pStack) {
            return this.countItem(pStack.getItem()) == 0;
        }
    }

    private static class NotPickUpSlot extends Slot {

        public NotPickUpSlot(Container pContainer, int pSlot, int pX, int pY) {
            super(pContainer, pSlot, pX, pY);
        }

        @Override
        public boolean mayPickup(Player pPlayer) {
            return this.getSlotIndex() != pPlayer.getInventory().selected;
        }
    }
}
