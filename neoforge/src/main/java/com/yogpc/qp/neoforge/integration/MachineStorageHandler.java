package com.yogpc.qp.neoforge.integration;

import com.yogpc.qp.machine.MachineStorage;
import com.yogpc.qp.machine.MachineStorageHolder;
import com.yogpc.qp.neoforge.TransferNeoForge;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public record MachineStorageHandler<T>(MachineStorageHolder<T> holder, T object)
    implements IItemHandler, IFluidHandler {
    private MachineStorage storage() {
        return holder.getMachineStorage(object);
    }

    @Override
    public int getTanks() {
        // It must contain an empty tank for new fluid
        return storage().fluidTanks() + 1;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        var f = storage().getFluidByIndex(tank);
        return TransferNeoForge.toForge(f);
    }

    @Override
    public int getTankCapacity(int tank) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return true;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (action.execute()) {
            storage().addFluid(resource.getFluid(), TransferNeoForge.toCommon(resource).amount().commonAmount());
        }
        return resource.getAmount();
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        var drained = storage().drainFluid(TransferNeoForge.toCommon(resource), action.execute());
        return TransferNeoForge.toForge(drained);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        var drained = storage().drainFluidByIndex(0, TransferNeoForge.toCommonAmount(maxDrain), action.execute());
        return TransferNeoForge.toForge(drained);
    }

    @Override
    public int getSlots() {
        // It must contain an empty tank for new item
        return storage().itemSlots() + 1;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return storage().getItemByIndex(slot);
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (!simulate) {
            storage().addItem(stack);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        return storage().extractItemByIndex(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return true;
    }

}
