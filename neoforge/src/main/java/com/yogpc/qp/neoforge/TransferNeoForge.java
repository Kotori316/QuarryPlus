package com.yogpc.qp.neoforge;

import com.yogpc.qp.FluidStackLike;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.neoforge.machine.MachineStorageNeoForge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public final class TransferNeoForge implements PlatformAccess.Transfer {
    @Override
    public ItemStack transferItem(Level level, BlockPos pos, ItemStack stack, Direction side, boolean simulate) {
        if (stack.isEmpty()) return stack;
        var handler = level.getCapability(Capabilities.Item.BLOCK, pos, side);
        if (handler == null) return stack;

        try (var transaction = Transaction.openRoot()) {
            var inserted = handler.insert(ItemResource.of(stack), stack.getCount(), transaction);
            if (!simulate) {
                transaction.commit();
            }
            if (stack.getCount() <= inserted) {
                return ItemStack.EMPTY;
            } else {
                return stack.copyWithCount(stack.getCount() - inserted);
            }
        }
    }

    @Override
    public FluidStackLike transferFluid(Level level, BlockPos pos, FluidStackLike stack, Direction side, boolean simulate) {
        if (stack.isEmpty()) return stack;
        var handler = level.getCapability(Capabilities.Fluid.BLOCK, pos, side);
        if (handler == null) return stack;

        var fluidStack = MachineStorageNeoForge.toForge(stack);
        var fluidResource = FluidResource.of(fluidStack);
        if (fluidResource.isEmpty()) return stack;

        try (var transaction = Transaction.openRoot()) {
            var inserted = handler.insert(fluidResource, fluidStack.getAmount(), transaction);
            if (!simulate) {
                transaction.commit();
            }
            if (inserted == 0) {
                return stack;
            } else if (stack.amount() <= inserted) {
                return FluidStackLike.EMPTY;
            } else {
                return stack.withAmount(stack.amount() - MachineStorageNeoForge.fromForgeAmount(inserted));
            }
        }
    }
}
