package com.yogpc.qp;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/**
 * @param amount The unit is Fabric one
 */
public record FluidStackLike(Fluid fluid, long amount, DataComponentPatch patch) {
    public static final FluidStackLike EMPTY = new FluidStackLike(Fluids.EMPTY, 0, DataComponentPatch.EMPTY);

    public boolean isEmpty() {
        return this.fluid == Fluids.EMPTY || this.amount == 0;
    }

    public FluidStackLike withAmount(long amount) {
        return new FluidStackLike(this.fluid, amount, this.patch);
    }
}
