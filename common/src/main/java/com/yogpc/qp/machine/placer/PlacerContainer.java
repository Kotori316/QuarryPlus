package com.yogpc.qp.machine.placer;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.misc.SlotContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class PlacerContainer extends AbstractContainerMenu {
    public static final String PLACER_GUI_NAME = "gui_" + PlacerBlock.NAME;
    public static final String REMOTE_PLACER_GUI_NAME = "gui_" + RemotePlacerBlock.NAME;
    public static final String PLACER_GUI_ID = QuarryPlus.modID + ":" + PLACER_GUI_NAME;
    public static final String REMOTE_PLACER_GUI_ID = QuarryPlus.modID + ":" + REMOTE_PLACER_GUI_NAME;
    private static final int PLACER_START_X = 62;
    private static final int REMOTE_PLACER_START_X = 26;
    @NotNull
    final AbstractPlacerTile tile;
    final int startX;

    private PlacerContainer(@Nullable MenuType<?> menuType, int containerId, Player player, BlockPos pos, int startX) {
        super(menuType, containerId);
        this.startX = startX;
        tile = (AbstractPlacerTile) Objects.requireNonNull(player.level().getBlockEntity(pos));

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                this.addSlot(new SlotContainer(this.tile, j + i * 3, startX + j * 18, 17 + i * 18));
            }
        }

        for (int k = 0; k < 3; ++k) {
            for (int i1 = 0; i1 < 9; ++i1) {
                this.addSlot(new Slot(player.getInventory(), i1 + k * 9 + 9, 8 + i1 * 18, 84 + k * 18));
            }
        }

        for (int l = 0; l < 9; ++l) {
            this.addSlot(new Slot(player.getInventory(), l, 8 + l * 18, 142));
        }

        if (!player.level().isClientSide()) {
            tile.syncToClient();
        }
    }

    public static PlacerContainer createPlacerContainer(int containerId, Inventory inventory, BlockPos pos) {
        return new PlacerContainer(PlatformAccess.getAccess().registerObjects().placerContainer().get(), containerId, inventory.player, pos, PLACER_START_X);
    }

    public static PlacerContainer createRemotePlacerContainer(int containerId, Inventory inventory, BlockPos pos) {
        return new PlacerContainer(PlatformAccess.getAccess().registerObjects().remotePlacerContainer().get(), containerId, inventory.player, pos, REMOTE_PLACER_START_X);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        var allSlots = tile.getContainerSize();
        Slot slot = this.getSlot(index);
        if (slot.hasItem()) {
            ItemStack remain = slot.getItem();
            ItemStack slotContent = remain.copy();
            if (index < allSlots) {
                if (!this.moveItemStackTo(remain, allSlots, 36 + allSlots, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(remain, 0, allSlots, false)) {
                return ItemStack.EMPTY;
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

    @Override
    public boolean stillValid(Player player) {
        return this.tile.stillValid(player);
    }
}
