package com.yogpc.qp.machine.storage;

import com.yogpc.qp.PlatformAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public final class DebugStorageContainer extends AbstractContainerMenu {
    public static final String NAME = "gui_" + DebugStorageBlock.NAME;
    final DebugStorageEntity storage;

    public DebugStorageContainer(int syncId, Inventory inventory, BlockPos pos) {
        super(PlatformAccess.getAccess().registerObjects().debugStorageContainer().get(), syncId);
        storage = Objects.requireNonNull((DebugStorageEntity) inventory.player.level().getBlockEntity(pos));
        int inventoryX = 29;
        int inventoryY = 107;
        int oneBox = 18;
        for (int h = 0; h < 3; h++) {
            for (int v = 0; v < 9; v++) {
                this.addSlot(new Slot(inventory, v + h * 9 + 9, inventoryX + v * oneBox, inventoryY + h * oneBox));
            }
        }
        for (int vertical = 0; vertical < 9; vertical++) {
            this.addSlot(new Slot(inventory, vertical, inventoryX + vertical * oneBox, inventoryY + 58));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return storage.getBlockPos().closerToCenterThan(player.position(), 8);
    }
}
