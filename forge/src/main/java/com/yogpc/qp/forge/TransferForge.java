package com.yogpc.qp.forge;

import com.yogpc.qp.PlatformAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.ItemHandlerHelper;

public final class TransferForge implements PlatformAccess.Transfer {
    @Override
    public ItemStack transferItem(Level level, BlockPos pos, ItemStack stack, Direction side, boolean simulate) {
        var entity = level.getBlockEntity(pos);
        if (entity == null) return stack;

        return entity.getCapability(ForgeCapabilities.ITEM_HANDLER, side)
            .map(h -> ItemHandlerHelper.insertItem(h, stack, simulate))
            .orElse(stack);
    }
}