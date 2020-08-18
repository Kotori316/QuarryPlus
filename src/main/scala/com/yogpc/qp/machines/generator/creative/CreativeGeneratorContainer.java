package com.yogpc.qp.machines.generator.creative;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.utils.Holder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class CreativeGeneratorContainer extends Container {
    public static final String GUI_ID = QuarryPlus.modID + ":gui_" + QuarryPlus.Names.creative_generator;
    public final CreativeGeneratorTile generatorTile;

    public CreativeGeneratorContainer(int id, PlayerEntity player, BlockPos pos) {
        super(Holder.creativeGeneratorContainerType(), id);
        generatorTile = (CreativeGeneratorTile) player.getEntityWorld().getTileEntity(pos);

        int oneBox = 18;
        for (int h = 0; h < 3; h++) {
            for (int v = 0; v < 9; v++) {
                this.addSlot(new Slot(player.inventory, v + h * 9 + 9, 8 + v * oneBox, 84 + h * oneBox));
            }
        }
        for (int vertical = 0; vertical < 9; vertical++) {
            this.addSlot(new Slot(player.inventory, vertical, 8 + vertical * oneBox, 142));
        }
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        return ItemStack.EMPTY;
    }
}
