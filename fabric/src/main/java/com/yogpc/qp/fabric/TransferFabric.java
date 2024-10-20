package com.yogpc.qp.fabric;

import com.yogpc.qp.FluidStackLike;
import com.yogpc.qp.PlatformAccess;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

final class TransferFabric implements PlatformAccess.Transfer {
    @Override
    public ItemStack transferItem(Level level, BlockPos pos, ItemStack stack, Direction side, boolean simulate) {
        if (stack.isEmpty()) return stack;
        var storage = ItemStorage.SIDED.find(level, pos, side);
        if (storage == null || !storage.supportsInsertion()) {
            // Nothing to insert
            return stack;
        }
        try (var transaction = Transaction.openOuter()) {
            var inserted = storage.insert(ItemVariant.of(stack), stack.getCount(), transaction);
            if (!simulate) {
                transaction.commit();
            }
            if (stack.getCount() <= inserted) {
                return ItemStack.EMPTY;
            } else {
                return stack.copyWithCount((int) (stack.getCount() - inserted));
            }
        }
    }

    @Override
    public FluidStackLike transferFluid(Level level, BlockPos pos, FluidStackLike stack, Direction side, boolean simulate) {
        if (stack.isEmpty()) return stack;
        var storage = FluidStorage.SIDED.find(level, pos, side);
        if (storage == null || !storage.supportsInsertion()) {
            // Nothing to insert
            return stack;
        }
        try (var transaction = Transaction.openOuter()) {
            var inserted = storage.insert(FluidVariant.of(stack.fluid(), stack.patch()), stack.amount(), transaction);
            if (!simulate) {
                transaction.commit();
            }
            if (inserted == 0) {
                return stack;
            } else if (stack.amount() <= inserted) {
                return FluidStackLike.EMPTY;
            } else {
                return new FluidStackLike(stack.fluid(), stack.amount() - inserted, stack.patch());
            }
        }
    }
}
