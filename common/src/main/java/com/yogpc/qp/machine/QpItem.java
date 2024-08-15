package com.yogpc.qp.machine;

import com.yogpc.qp.InCreativeTabs;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public abstract class QpItem extends Item implements InCreativeTabs {
    public final ResourceLocation name;

    public QpItem(Properties properties, String name) {
        super(properties);
        this.name = ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, name);
    }
}
