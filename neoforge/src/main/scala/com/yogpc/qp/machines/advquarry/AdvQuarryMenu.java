package com.yogpc.qp.machines.advquarry;

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

import java.util.Objects;

public class AdvQuarryMenu extends AbstractContainerMenu {
    public static final String GUI_ID = QuarryPlus.modID + ":gui_" + BlockAdvQuarry.NAME;
    final TileAdvQuarry quarry;
    final int imageWidth;
    final int imageHeight;

    public AdvQuarryMenu(int id, Player player, BlockPos pos) {
        super(Holder.ADV_QUARRY_MENU_TYPE, id);
        quarry = Objects.requireNonNull((TileAdvQuarry) player.level().getBlockEntity(pos),
            "Tile at %s in %s is null".formatted(pos, player.level().dimension()));
        this.imageWidth = 176;
        this.imageHeight = 200;

        for (int k = 0; k < 3; ++k) {
            for (int i1 = 0; i1 < 9; ++i1) {
                this.addSlot(new Slot(player.getInventory(), i1 + k * 9 + 9, 8 + i1 * 18, this.imageHeight - 82 + k * 18));
            }
        }

        for (int l = 0; l < 9; ++l) {
            this.addSlot(new Slot(player.getInventory(), l, 8 + l * 18, this.imageHeight - 24));
        }
        if (!player.level().isClientSide) {
            PacketHandler.sendToClientPlayer(new ClientSyncMessage(quarry), (ServerPlayer) player);
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
