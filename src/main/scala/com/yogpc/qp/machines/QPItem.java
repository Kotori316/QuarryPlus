package com.yogpc.qp.machines;

import java.util.Objects;

import net.minecraft.world.item.Item;

public class QPItem extends Item {
    public QPItem(Item.Properties properties) {
        super(properties);
    }

    /**
     * Implemented for unit test. Default implementation just returns "air".
     * This override return actual item name.
     */
    @Override
    public String toString() {
        return Objects.requireNonNull(getRegistryName()).getPath();
    }
}
