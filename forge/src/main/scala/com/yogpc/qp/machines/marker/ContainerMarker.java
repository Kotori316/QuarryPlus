package com.yogpc.qp.machines.marker;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerMarker extends AbstractContainerMenu {

    final Player player;
    final BlockPos pos;

    public ContainerMarker(int id, Player player, BlockPos pos, MenuType<?> type, int inventoryX, int inventoryY) {
        super(type, id);
        this.player = player;
        this.pos = pos;
        int oneBox = 18;
        for (int h = 0; h < 3; h++) {
            for (int v = 0; v < 9; v++) {
                this.addSlot(new Slot(player.getInventory(), v + h * 9 + 9, inventoryX + v * oneBox, inventoryY + h * oneBox));
            }
        }
        for (int vertical = 0; vertical < 9; vertical++) {
            this.addSlot(new Slot(player.getInventory(), vertical, inventoryX + vertical * oneBox, inventoryY + 58));
        }
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        return ItemStack.EMPTY;
    }
}
