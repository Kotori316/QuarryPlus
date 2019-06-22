package com.yogpc.qp.machines.base;

import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;

public interface HasStorage {
    Storage getStorage();

    interface Storage {
        void insertItem(ItemStack stack);

        void insertFluid(Fluid fluid, long amount);
    }
}
