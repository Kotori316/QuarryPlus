package com.yogpc.qp.machine.module;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryDataComponents;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.MachineStorage;
import com.yogpc.qp.machine.misc.SlotContainer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public final class FilterModuleContainer extends AbstractContainerMenu {
    public static final String NAME = "gui_" + FilterModuleItem.NAME;
    public static final String GUI_ID = QuarryPlus.modID + ":" + NAME;
    final int containerRows;
    final int allSlots;
    private final ContainerInv container;

    public FilterModuleContainer(int id, Inventory inventory, ItemStack filterModuleItem) {
        super(PlatformAccess.getAccess().registerObjects().filterModuleContainer().get(), id);
        var player = inventory.player;
        this.container = new ContainerInv(filterModuleItem);
        this.containerRows = 2;
        this.allSlots = containerRows * 9;
        int i = (containerRows - 4) * 18;

        final int oneBox = 18;
        // Filter item inventory
        for (int j = 0; j < containerRows; ++j) {
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
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack slotItem = slot.getItem();
            ItemStack copy = slotItem.copy();
            if (index < this.allSlots) {
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
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }

    private static class ContainerInv extends SimpleContainer {
        private final ItemStack filterModuleItem;

        public ContainerInv(ItemStack filterModuleItem) {
            super(18);
            this.filterModuleItem = filterModuleItem;
            Optional.ofNullable(filterModuleItem.get(QuarryDataComponents.ITEM_KEY_LIST_COMPONENT))
                .stream()
                .flatMap(Collection::stream)
                .map(itemKey -> itemKey.toStack(1))
                .forEach(this::addItem);
        }

        @Override
        public final int getMaxStackSize() {
            return 1;
        }

        @Override
        public void stopOpen(Player player) {
            super.stopOpen(player);
            if (this.isEmpty()) {
                filterModuleItem.remove(QuarryDataComponents.ITEM_KEY_LIST_COMPONENT);
            } else {
                List<MachineStorage.ItemKey> itemKeys = getItems().stream()
                    .map(MachineStorage.ItemKey::of)
                    .toList();
                filterModuleItem.set(QuarryDataComponents.ITEM_KEY_LIST_COMPONENT, itemKeys);
            }
        }

        @Override
        public boolean canPlaceItem(int pIndex, ItemStack pStack) {
            // Not to allow to put same item twice
            return this.countItem(pStack.getItem()) == 0;
        }
    }

    private static class NotPickUpSlot extends Slot {

        public NotPickUpSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPickup(Player player) {
            return this.getContainerSlot() != player.getInventory().selected;
        }
    }
}
