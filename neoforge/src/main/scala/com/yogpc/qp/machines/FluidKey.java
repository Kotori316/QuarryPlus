package com.yogpc.qp.machines;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.Nullable;

public record FluidKey(Fluid fluid, @Nullable CompoundTag nbt) {
    public FluidKey(FluidStack stack) {
        this(stack.getFluid(), stack.getTag());
    }

    public CompoundTag createNbt(long amount) {
        var tag = new CompoundTag();
        tag.putString("fluid", getId().toString());
        if (nbt != null)
            tag.put("tag", nbt);
        tag.putLong("amount", amount);
        return tag;
    }

    static FluidKey fromNbt(CompoundTag tag) {
        var fluid = BuiltInRegistries.FLUID.get(new ResourceLocation(tag.getString("fluid")));
        var nbt = tag.contains("nbt") ? tag.getCompound("nbt") : null;
        return new FluidKey(fluid, nbt);
    }

    public FluidStack toStack(int amount) {
        return new FluidStack(this.fluid(), amount, this.nbt());
    }

    public ResourceLocation getId() {
        return BuiltInRegistries.FLUID.getKey(fluid);
    }
}
