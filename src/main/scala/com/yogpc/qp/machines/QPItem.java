package com.yogpc.qp.machines;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class QPItem extends Item {
    private ResourceLocation internalName;

    public QPItem(Item.Properties properties) {
        super(properties);
    }

    public void setRegistryName(String modId, String name) {
        internalName = new ResourceLocation(modId, name);
    }

    /**
     * Implemented for unit test. Default implementation just returns "air".
     * This override return actual item name.
     */
    @Override
    public String toString() {
        return internalName.getPath();
    }
}
