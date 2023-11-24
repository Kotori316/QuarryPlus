package com.yogpc.qp.machines;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

public class QPItem extends Item implements InCreativeTabs {
    private final ResourceLocation internalName;

    public QPItem(Item.Properties properties, String path) {
        super(properties);
        this.internalName = new ResourceLocation(QuarryPlus.modID, path);
    }

    @NotNull
    public ResourceLocation getRegistryName() {
        return internalName;
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
