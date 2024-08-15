package com.yogpc.qp.machine.misc;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class YSetterContainer extends AbstractContainerMenu {
    public static final String GUI_ID = QuarryPlus.modID + ":gui_y_setter";
    @Nullable
    final YAccessor<?> yAccessor;

    public YSetterContainer(int syncId, Inventory inventory, BlockPos pos) {
        super(PlatformAccess.getAccess().registerObjects().ySetterContainer().get(), syncId);
        var player = inventory.player;
        this.yAccessor = YAccessor.get(player.level().getBlockEntity(pos));

        final int oneBox = 18;
        for (int h = 0; h < 3; h++) {
            for (int v = 0; v < 9; v++) {
                addSlot(new Slot(player.getInventory(), v + h * 9 + 9, 8 + v * oneBox, 84 + h * oneBox));
            }
        }
        for (int vertical = 0; vertical < 9; vertical++) {
            addSlot(new Slot(player.getInventory(), vertical, 8 + vertical * oneBox, 142));
        }

        if (this.yAccessor != null) {
            this.yAccessor.entity().syncToClient();
        }
    }

    @Override
    public boolean stillValid(Player playerIn) {
        if (yAccessor == null) {
            return false;
        }
        return yAccessor.entity().getBlockPos().distToCenterSqr(playerIn.position()) < 64;
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        return ItemStack.EMPTY;
    }
}
