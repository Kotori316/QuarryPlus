package com.yogpc.qp.forge.integration;

import com.yogpc.qp.FluidStackLike;
import com.yogpc.qp.machine.MachineStorage;
import com.yogpc.qp.machine.MachineStorageHolder;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentPatch;
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

    static FluidStack toForge(FluidStackLike f) {
        return new FluidStack(f.fluid(), Math.clamp(f.amount() * 1000 / MachineStorage.ONE_BUCKET, 0, Integer.MAX_VALUE));
    }

    static FluidStackLike toCommon(FluidStack f) {
        return new FluidStackLike(f.getFluid(), (long) f.getAmount() * MachineStorage.ONE_BUCKET / 1000, DataComponentPatch.EMPTY);
    }

    @Override
    public int getTanks() {
        // It must contain an empty tank for new fluid
        return storage().fluidTanks() + 1;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        var f = storage().getFluidByIndex(tank);
        return toForge(f);
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
        var drained = storage().drainFluid(toCommon(resource), action.execute());
        return toForge(drained);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        var drained = storage().drainFluidByIndex(0, maxDrain, action.execute());
        return toForge(drained);
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
