package com.yogpc.qp.machine.module;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.misc.SlotContainer;
import com.yogpc.qp.machine.misc.YAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class ModuleContainer extends AbstractContainerMenu {
    public static final String GUI_ID = QuarryPlus.modID + ":gui_quarry_module";
    @Nullable
    final ModuleInventory moduleInventory;
    @Nullable
    final YAccessor<?> yAccessor;

    public ModuleContainer(int id, Inventory inventory, BlockPos pos) {
        super(PlatformAccess.getAccess().registerObjects().moduleContainer().get(), id);
        var entity = inventory.player.level().getBlockEntity(pos);
        moduleInventory = ModuleInventoryHolder.getFromObject(entity).orElse(null);
        yAccessor = YAccessor.get(entity);
        final int oneBox = 18;

        if (moduleInventory != null) {
            for (int i = 0; i < moduleInventory.getContainerSize(); ++i) {
                int verticalFix = i < 5 ? i : i - 5;
                int horizontalFix = i / 5;
                addSlot(new SlotContainer(moduleInventory, i, 44 + verticalFix * oneBox, 27 + horizontalFix * oneBox));
            }
        }

        for (int h = 0; h < 3; h++) {
            for (int v = 0; v < 9; v++) {
                this.addSlot(new Slot(inventory, v + h * 9 + 9, 8 + v * oneBox, 84 + h * oneBox));
            }
        }
        for (int vertical = 0; vertical < 9; vertical++) {
            this.addSlot(new Slot(inventory, vertical, 8 + vertical * oneBox, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        int allSlots = moduleInventory != null ? moduleInventory.getContainerSize() : 0;

        Slot slot = this.getSlot(index);
        if (slot.hasItem()) {
            ItemStack remain = slot.getItem();
            ItemStack slotContent = remain.copy();
            if (index < allSlots) {
                if (!this.moveItemStackTo(remain, allSlots, 36 + allSlots, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(remain, 0, allSlots, false)) {
                return ItemStack.EMPTY;
            }

            if (remain.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (remain.getCount() == slotContent.getCount()) {
                // Nothing moved
                return ItemStack.EMPTY;
            }

            slot.onTake(player, remain);
            return slotContent;
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
