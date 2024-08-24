package com.yogpc.qp.machine.marker;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.machine.QpEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class MarkerContainer extends AbstractContainerMenu {
    public static final String FLEXIBLE_NAME = "gui_flexible_marker";
    public static final String CHUNK_NAME = "gui_chunk_marker";

    final Player player;
    final BlockPos pos;
    final QpEntity entity;

    private MarkerContainer(MenuType<? extends MarkerContainer> type, int id, Inventory inventory, BlockPos pos, int inventoryX, int inventoryY) {
        super(type, id);
        this.player = inventory.player;
        this.pos = pos;
        this.entity = ((QpEntity) player.level().getBlockEntity(pos));
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
    public boolean stillValid(Player playerIn) {
        return pos.closerToCenterThan(playerIn.position(), 8);
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        return ItemStack.EMPTY;
    }

    public static MarkerContainer createFlexibleMarkerContainer(int id, Inventory inventory, BlockPos pos) {
        return new MarkerContainer(PlatformAccess.getAccess().registerObjects().flexibleMarkerContainer().get(), id, inventory, pos, 29, 139);
    }

    public static MarkerContainer createChunkMarkerContainer(int id, Inventory inventory, BlockPos pos) {
        return new MarkerContainer(PlatformAccess.getAccess().registerObjects().chunkMarkerContainer().get(), id, inventory, pos, 29, 107);
    }
}
