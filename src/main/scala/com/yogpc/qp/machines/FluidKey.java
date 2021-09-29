package com.yogpc.qp.machines;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

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
        var fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(tag.getString("fluid")));
        var nbt = tag.contains("nbt") ? tag.getCompound("nbt") : null;
        return new FluidKey(fluid, nbt);
    }

    public FluidStack toStack(int amount) {
        return new FluidStack(this.fluid(), amount, this.nbt());
    }

    public ResourceLocation getId() {
        return ForgeRegistries.FLUIDS.getKey(fluid);
    }
}
