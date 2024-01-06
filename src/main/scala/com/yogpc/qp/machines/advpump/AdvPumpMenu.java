package com.yogpc.qp.machines.advpump;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.ClientSyncMessage;
import com.yogpc.qp.packet.PacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class AdvPumpMenu extends AbstractContainerMenu {
    public static final String GUI_ID = QuarryPlus.modID + ":gui_" + BlockAdvPump.NAME;
    @NotNull
    final TileAdvPump pump;

    public AdvPumpMenu(int id, Player player, BlockPos pos) {
        super(Holder.ADV_PUMP_MENU_TYPE, id);
        this.pump = Objects.requireNonNull(Holder.ADV_PUMP_TYPE.getBlockEntity(player.level, pos));

        final int oneBox = 18;
        for (int h = 0; h < 3; h++) {
            for (int v = 0; v < 9; v++) {
                this.addSlot(new Slot(player.getInventory(), v + h * 9 + 9, 8 + v * oneBox, 84 + h * oneBox));
            }
        }
        for (int vertical = 0; vertical < 9; vertical++) {
            this.addSlot(new Slot(player.getInventory(), vertical, 8 + vertical * oneBox, 142));
        }
        if (!player.level.isClientSide) {
            PacketHandler.sendToClientPlayer(new ClientSyncMessage(this.pump), (ServerPlayer) player);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.pump.getBlockPos().distToCenterSqr(player.position()) < 64;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }
}
