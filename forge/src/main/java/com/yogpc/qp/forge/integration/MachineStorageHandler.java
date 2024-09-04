package com.yogpc.qp.forge.integration;

import com.yogpc.qp.machine.MachineStorage;
import com.yogpc.qp.machine.MachineStorageHolder;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record MachineStorageHandler<T>(MachineStorageHolder<T> holder, T object)
    implements IItemHandler, IFluidHandler, ICapabilityProvider {
    private MachineStorage storage() {
        return holder.getMachineStorage(object);
    }

    @Override
    public int getTanks() {
        return storage().fluidTanks();
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        var f = storage().getFluidByIndex(tank);
        return new FluidStack(f.fluid(), Math.clamp(f.amount(), 0, Integer.MAX_VALUE));
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
            storage().addFluid(resource.getFluid(), resource.getAmount());
        }
        return resource.getAmount();
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        return FluidStack.EMPTY;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        return FluidStack.EMPTY;
    }

    @Override
    public int getSlots() {
        return storage().itemSlots();
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
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return true;
    }

    @Override
    public @NotNull <S> LazyOptional<S> getCapability(@NotNull Capability<S> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return ForgeCapabilities.ITEM_HANDLER.orEmpty(cap, LazyOptional.of(() -> this));
        }
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return ForgeCapabilities.FLUID_HANDLER.orEmpty(cap, LazyOptional.of(() -> this));
        }
        return LazyOptional.empty();
    }
}
