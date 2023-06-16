package com.yogpc.qp.machines.misc;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class YSetterContainer extends AbstractContainerMenu {
    public static final ResourceLocation GUI_ID = new ResourceLocation(QuarryPlus.modID, "gui_y_setter");
    @Nullable
    final YAccessor yAccessor;

    public YSetterContainer(int syncId, Player player, BlockPos pos) {
        super(QuarryPlus.ModObjects.Y_SETTER_HANDLER_TYPE, syncId);
        yAccessor = YAccessor.get(player.level().getBlockEntity(pos));

        final int oneBox = 18;
        for (int h = 0; h < 3; h++) {
            for (int v = 0; v < 9; v++) {
                addSlot(new Slot(player.getInventory(), v + h * 9 + 9, 8 + v * oneBox, 84 + h * oneBox));
            }
        }
        for (int vertical = 0; vertical < 9; vertical++) {
            addSlot(new Slot(player.getInventory(), vertical, 8 + vertical * oneBox, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
