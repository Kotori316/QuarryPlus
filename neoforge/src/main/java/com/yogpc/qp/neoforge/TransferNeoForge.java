package com.yogpc.qp.neoforge;

import com.yogpc.qp.FluidStackLike;
import com.yogpc.qp.PlatformAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public final class TransferNeoForge implements PlatformAccess.Transfer {
    @Override
    public ItemStack transferItem(Level level, BlockPos pos, ItemStack stack, Direction side, boolean simulate) {
        var handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, side);
        if (handler == null) return stack;

        return ItemHandlerHelper.insertItem(handler, stack, simulate);
    }

    @Override
    public FluidStackLike transferFluid(Level level, BlockPos pos, FluidStackLike stack, Direction side, boolean simulate) {
        var handler = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, side);
        if (handler == null) return stack;

        var inserted = handler.fill(new FluidStack(Holder.direct(stack.fluid()), (int) stack.amount(), stack.patch()), simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
        if (inserted == 0) return stack;
        return new FluidStackLike(stack.fluid(), stack.amount() - inserted, stack.patch());
    }
}
