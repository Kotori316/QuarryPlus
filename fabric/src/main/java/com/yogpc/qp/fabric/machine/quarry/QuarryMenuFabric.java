package com.yogpc.qp.fabric.machine.quarry;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.fabric.PlatformAccessFabric;
import com.yogpc.qp.machine.quarry.QuarryBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class QuarryMenuFabric extends AbstractContainerMenu {
    public static final String GUI_ID = QuarryPlus.modID + ":" + "gui_" + QuarryBlock.NAME;
    @NotNull
    final QuarryEntityFabric quarry;

    public QuarryMenuFabric(int id, Inventory inventory, BlockPos pos) {
        super(PlatformAccessFabric.RegisterObjectsFabric.QUARRY_MENU, id);
        this.quarry = Objects.requireNonNull((QuarryEntityFabric) inventory.player.level().getBlockEntity(pos));
        final int oneBox = 18;
        for (int h = 0; h < 3; h++) {
            for (int v = 0; v < 9; v++) {
                addSlot(new Slot(inventory, v + h * 9 + 9, 8 + v * oneBox, 84 + h * oneBox));
            }
        }
        for (int vertical = 0; vertical < 9; vertical++) {
            addSlot(new Slot(inventory, vertical, 8 + vertical * oneBox, 142));
        }
        quarry.syncToClient();
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
