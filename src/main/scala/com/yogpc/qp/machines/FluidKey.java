package com.yogpc.qp.machines;

import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record FluidKey(Fluid fluid, @Nullable NbtCompound nbt) {

    public NbtCompound createNbt(long amount) {
        var tag = new NbtCompound();
        tag.putString("fluid", getId().toString());
        if (nbt != null)
            tag.put("tag", nbt);
        tag.putLong("amount", amount);
        return tag;
    }

    static FluidKey fromNbt(NbtCompound tag) {
        var fluid = Registry.FLUID.get(new Identifier(tag.getString("fluid")));
        var nbt = tag.contains("nbt") ? tag.getCompound("nbt") : null;
        return new FluidKey(fluid, nbt);
    }

    @NotNull
    public Identifier getId() {
        return Registry.FLUID.getId(fluid);
    }
}
