package com.yogpc.qp;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/**
 * @param amount The unit is Fabric one
 */
public record FluidStackLike(Fluid fluid, FluidStackLikeUnit amount, DataComponentPatch patch) {
    public static final FluidStackLike EMPTY = new FluidStackLike(Fluids.EMPTY, FluidStackLikeUnit.ZERO, DataComponentPatch.EMPTY);

    public boolean isEmpty() {
        return this.fluid == Fluids.EMPTY || this.amount.commonAmount() == 0;
    }

    public FluidStackLike withAmount(FluidStackLikeUnit amount) {
        return new FluidStackLike(this.fluid, amount, this.patch);
    }
}
