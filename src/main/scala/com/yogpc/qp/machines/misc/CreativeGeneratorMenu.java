package com.yogpc.qp.machines.misc;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.TileMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CreativeGeneratorMenu extends AbstractContainerMenu {
    public static final String GUI_ID = QuarryPlus.modID + ":gui_" + CreativeGeneratorBlock.NAME;
    @NotNull
    final CreativeGeneratorTile tile;

    public CreativeGeneratorMenu(int id, Player player, BlockPos pos) {
        super(Holder.CREATIVE_GENERATOR_MENU_TYPE, id);
        tile = (CreativeGeneratorTile) player.level.getBlockEntity(pos);
        for (int k = 0; k < 3; ++k) {
            for (int i1 = 0; i1 < 9; ++i1) {
                this.addSlot(new Slot(player.getInventory(), i1 + k * 9 + 9, 8 + i1 * 18, 84 + k * 18));
            }
        }

        for (int l = 0; l < 9; ++l) {
            this.addSlot(new Slot(player.getInventory(), l, 8 + l * 18, 142));
        }
        if (!player.level.isClientSide && tile != null) {
            PacketHandler.sendToClientPlayer(new TileMessage(tile), (ServerPlayer) player);
        }
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return true;
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public ItemStack quickMoveStack(Player player, int index) {
        assert index >= 0;
        Slot slot = this.getSlot(index);
        if (slot.hasItem()) {
            ItemStack remain = slot.getItem();
            ItemStack slotContent = remain.copy();
            if (index < 27) {
                if (!this.moveItemStackTo(remain, 27, 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < 36) {
                if (!this.moveItemStackTo(remain, 0, 27, false)) {
                    return ItemStack.EMPTY;
                }
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
}
