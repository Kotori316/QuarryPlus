package com.yogpc.qp.machines.quarry;

import java.util.Objects;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class QuarryMenu extends AbstractContainerMenu {
    public static final String GUI_ID = QuarryPlus.modID + ":" + "gui_" + BlockQuarry.NAME;
    @NotNull
    final TileQuarry quarry;

    public QuarryMenu(int id, Player player, BlockPos pos) {
        super(QuarryPlus.ModObjects.QUARRY_MENU_TYPE, id);
        this.quarry = Objects.requireNonNull((TileQuarry) player.level.getBlockEntity(pos));
        final int oneBox = 18;
        for (int h = 0; h < 3; h++) {
            for (int v = 0; v < 9; v++) {
                addSlot(new Slot(player.getInventory(), v + h * 9 + 9, 8 + v * oneBox, 84 + h * oneBox));
            }
        }
        for (int vertical = 0; vertical < 9; vertical++) {
            addSlot(new Slot(player.getInventory(), vertical, 8 + vertical * oneBox, 142));
        }
        if (!player.level.isClientSide) {
            quarry.sync();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return quarry.getBlockPos().distToCenterSqr(player.position()) < 64;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }
}
