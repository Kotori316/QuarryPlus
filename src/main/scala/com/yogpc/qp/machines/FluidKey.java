package com.yogpc.qp.machines;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record FluidKey(Fluid fluid, @Nullable CompoundTag nbt) {

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

    @NotNull
    public ResourceLocation getId() {
        return BuiltInRegistries.FLUID.getKey(fluid);
    }
}
