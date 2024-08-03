package com.yogpc.qp.machine.misc;

import com.yogpc.qp.InCreativeTabs;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class YSetterItem extends Item implements InCreativeTabs {
    public static final String NAME = "y_setter";
    public final ResourceLocation name = ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, NAME);

    public YSetterItem() {
        super(new Properties());
    }
}
