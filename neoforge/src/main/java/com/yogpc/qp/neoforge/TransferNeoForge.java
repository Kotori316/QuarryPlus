package com.yogpc.qp.neoforge;

import com.yogpc.qp.PlatformAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public final class TransferNeoForge implements PlatformAccess.Transfer {
    @Override
    public ItemStack transferItem(Level level, BlockPos pos, ItemStack stack, Direction side, boolean simulate) {
        var handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, side);
        if (handler == null) return stack;

        return ItemHandlerHelper.insertItem(handler, stack, simulate);
    }
}
