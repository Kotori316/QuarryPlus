package com.yogpc.qp.machines.module;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.QPItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class PumpModuleItem extends QPItem implements QuarryModuleProvider.Item {
    public static final String NAME = "pump_module";

    public PumpModuleItem() {
        super(new ResourceLocation(QuarryPlus.modID, NAME), new Properties());
    }

    @Override
    public QuarryModule getModule(ItemStack stack) {
        return QuarryModule.Constant.PUMP;
    }
}
