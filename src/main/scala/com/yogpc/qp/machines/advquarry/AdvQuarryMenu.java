package com.yogpc.qp.machines.advquarry;

import java.util.Objects;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class AdvQuarryMenu extends AbstractContainerMenu {
    public static final String GUI_ID = QuarryPlus.modID + ":gui_" + BlockAdvQuarry.NAME;
    @NotNull
    final TileAdvQuarry quarry;

    public AdvQuarryMenu(int id, Player player, BlockPos pos) {
        super(QuarryPlus.ModObjects.ADV_QUARRY_MENU_TYPE, id);
        quarry = Objects.requireNonNull((TileAdvQuarry) player.level().getBlockEntity(pos));
        for (int k = 0; k < 3; ++k) {
            for (int i1 = 0; i1 < 9; ++i1) {
                this.addSlot(new Slot(player.getInventory(), i1 + k * 9 + 9, 8 + i1 * 18, 84 + k * 18));
            }
        }

        for (int l = 0; l < 9; ++l) {
            this.addSlot(new Slot(player.getInventory(), l, 8 + l * 18, 142));
        }
        if (!player.level().isClientSide) {
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
