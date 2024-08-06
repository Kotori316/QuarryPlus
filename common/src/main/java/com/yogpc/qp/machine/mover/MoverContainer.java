package com.yogpc.qp.machine.mover;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.misc.SlotContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public final class MoverContainer extends AbstractContainerMenu {
    public static final String GUI_ID = QuarryPlus.modID + ":gui_" + MoverBlock.NAME;
    final MoverEntity entity;

    public MoverContainer(int syncId, Inventory inventory, BlockPos pos) {
        super(PlatformAccess.getAccess().registerObjects().moverContainer().get(), syncId);
        entity = Objects.requireNonNull((MoverEntity) inventory.player.level().getBlockEntity(pos));

        int row;
        int col;
        for (col = 0; col < 2; ++col) {
            addSlot(new SlotContainer(this.entity.inventory, col, 8 + col * 144, 40));
        }

        for (row = 0; row < 3; ++row) {
            for (col = 0; col < 9; ++col) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 104 + row * 18));
            }
        }

        for (col = 0; col < 9; ++col) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 162));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return entity.getBlockPos().distToCenterSqr(player.position()) < 64;
    }
}
