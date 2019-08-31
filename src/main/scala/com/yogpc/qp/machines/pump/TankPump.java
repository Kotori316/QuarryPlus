package com.yogpc.qp.machines.pump;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.yogpc.qp.machines.base.HasStorage;
import com.yogpc.qp.machines.base.IDummyFluidHandler;
import com.yogpc.qp.utils.FluidElement;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class TankPump implements HasStorage.Storage, ICapabilityProvider {

    All all = new All();
    // The equal method of FluidStack checks the kind of fluid, not the amount of stack.
    Map<FluidElement, FluidStack> stacks = new HashMap<>();
    List<FluidElement> keys = new ArrayList<>();
    // The stacks in list doesn't show actual stack, just the kind.
    public EnumMap<Direction, List<FluidElement>> mapping = new EnumMap<>(Direction.class);
    EnumMap<Direction, PumpTank> pumpTankEnumMap = new EnumMap<>(Direction.class);

    public TankPump() {
        for (Direction direction : Direction.values()) {
            mapping.put(direction, new ArrayList<>());
            pumpTankEnumMap.put(direction, new PumpTank(direction));
        }
    }

    @Override
    public void insertItem(ItemStack stack) {
        // Actually pump does not receive any items.
    }

    @Override
    public void insertFluid(FluidStack fluidStack) {
        FluidElement key = FluidElement.fromStack(fluidStack);
        stacks.merge(key, fluidStack, (fluidStack1, fluidStack2) -> {
            FluidStack newOne = fluidStack1.copy();
            newOne.setAmount(newOne.getAmount() + fluidStack2.getAmount());
            return newOne;
        });
        if (!keys.contains(key)) {
            keys.add(key);
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            IFluidHandler handler;
            if (side == null) {
                handler = all;
            } else {
                handler = pumpTankEnumMap.get(side);
            }
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> handler));
        } else {
            return LazyOptional.empty();
        }
    }

    public Collection<FluidStack> getAllContents() {
        return Collections.unmodifiableCollection(stacks.values());
    }

    public CompoundNBT serializeNBT(boolean writeContents) {
        CompoundNBT allNbt = new CompoundNBT();
        CompoundNBT mappingNbt = new CompoundNBT();
        mapping.forEach((direction, fluidStacks) -> {
            ListNBT fss = fluidStacks.stream().map(FluidElement::toCompoundTag).collect(Collectors.toCollection(ListNBT::new));
            mappingNbt.put(direction.getName(), fss);
        });
        if (writeContents) {
            ListNBT keysNbt = keys.stream().map(FluidElement::toCompoundTag).collect(Collectors.toCollection(ListNBT::new));
            ListNBT stacksNbt = stacks.values().stream().map(fs -> fs.writeToNBT(new CompoundNBT())).collect(Collectors.toCollection(ListNBT::new));
            allNbt.put("keys", keysNbt);
            allNbt.put("stacks", stacksNbt);
        }
        allNbt.put("mapping", mappingNbt);

        return allNbt;
    }

    public void deserializeNBT(CompoundNBT allNbt, boolean readContents) {
        CompoundNBT mappingNbt = allNbt.getCompound("mapping");
        for (Direction direction : Direction.values()) {
            ListNBT fss = mappingNbt.getList(direction.getName(), Constants.NBT.TAG_COMPOUND);
            mapping.put(direction, fss.stream().map(CompoundNBT.class::cast).map(FluidElement::fromNBT).collect(Collectors.toList()));
        }
        if (readContents) {
            ListNBT keysNbt = allNbt.getList("keys", Constants.NBT.TAG_COMPOUND);
            ListNBT stacksNbt = allNbt.getList("stacks", Constants.NBT.TAG_COMPOUND);
            keys = keysNbt.stream().map(CompoundNBT.class::cast).map(FluidElement::fromNBT).collect(Collectors.toList());
            stacks.clear();
            stacksNbt.stream().map(CompoundNBT.class::cast)
                .map(FluidStack::loadFluidStackFromNBT)
                .filter(s -> keys.contains(FluidElement.fromStack(s)))
                .forEach(s -> stacks.put(FluidElement.fromStack(s), s));
        }
    }

    private class All implements IDummyFluidHandler {

        @Override
        public int getTanks() {
            return stacks.size();
        }

        @Nonnull
        @Override
        public FluidStack getFluidInTank(int tank) {
            return stacks.get(keys.get(tank));
        }

        @Override
        public int getTankCapacity(int tank) {
            return getFluidInTank(tank).getAmount();
        }

    }

    private class PumpTank implements IFluidHandler {
        final Direction facing;

        private PumpTank(Direction facing) {
            this.facing = facing;
        }

        private List<FluidElement> getList() {
            return mapping.get(facing);
        }

        @Override
        public int getTanks() {
            return getList().size();
        }

        @Nonnull
        @Override
        public FluidStack getFluidInTank(int tank) {
            return stacks.get(getList().get(tank));
        }

        @Override
        public int getTankCapacity(int tank) {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
            return true;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0; // Not fill-able.
        }

        @Nonnull
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            FluidElement key = FluidElement.fromStack(resource);
            if (resource.isEmpty() || !stacks.containsKey(key)) {
                return FluidStack.EMPTY;
            }
            FluidStack source = stacks.get(key);
            if (source.getAmount() > resource.getAmount()) {
                // just reduce the amount
                if (action.execute()) {
                    stacks.put(key, new FluidStack(source, source.getAmount() - resource.getAmount()));
                }
                return resource.copy();
            } else {
                // remove the fluid from list.
                if (action.execute()) {
                    stacks.remove(key);
                    keys.remove(key);
                }
                return source.copy();
            }
        }

        @Nonnull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            if (maxDrain <= 0 || stacks.isEmpty()) {
                return FluidStack.EMPTY;
            }
            FluidStack source;
            if (getList().isEmpty()) {
                // Drain from universal tank.
                source = stacks.get(keys.get(0));
            } else {
                // Drain from direction-separated tank.
                source = stacks.get(getList().get(0));
            }
            FluidStack resource = new FluidStack(source, maxDrain);
            return this.drain(resource, action);
        }
    }
}
