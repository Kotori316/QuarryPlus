package com.yogpc.qp.machines.base;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public interface HasStorage {
    Storage getStorage();

    /**
     * A storage that can hold all inserted items and fluids.
     * So, not like normal item handlers, these methods returns nothing, that means all items is accepted.
     */
    interface Storage {
        /**
         * Add the item to inventory. This inventory accepts all items that is inserted.
         *
         * @param stack to be inserted.
         */
        void insertItem(ItemStack stack);

        /**
         * Add the fluid to inventory.
         *
         * @param fluidStack to be inserted.
         */
        void insertFluid(FluidStack fluidStack);
    }

    interface HasDummyStorage extends HasStorage {
        class DummyStorage implements HasStorage.Storage {
            @Override
            public void insertItem(ItemStack stack) {
            }

            @Override
            public void insertFluid(FluidStack fluidStack) {
            }
        }

        HasStorage.Storage DUMMY = new DummyStorage();

        @Override
        default Storage getStorage() {
            return DUMMY;
        }
    }
}
