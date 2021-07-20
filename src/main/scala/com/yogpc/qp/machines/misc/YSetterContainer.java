package com.yogpc.qp.machines.misc;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class YSetterContainer extends ScreenHandler {
    public static final Identifier GUI_ID = new Identifier(QuarryPlus.modID, "gui_y_setter");
    @Nullable
    final YAccessor yAccessor;

    public YSetterContainer(int syncId, PlayerEntity player, BlockPos pos) {
        super(QuarryPlus.ModObjects.Y_SETTER_HANDLER_TYPE, syncId);
        yAccessor = YAccessor.get(player.world.getBlockEntity(pos));

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
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        return ItemStack.EMPTY;
    }
}
