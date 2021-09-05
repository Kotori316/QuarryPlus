package com.yogpc.qp.machines.module;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.QPItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class PumpModuleItem extends QPItem implements QuarryModuleProvider.Item {
    public static final String NAME = "pump_module";

    public PumpModuleItem() {
        super(new Properties().tab(Holder.TAB));
        setRegistryName(QuarryPlus.modID, NAME);
    }

    @Override
    public QuarryModule getModule(ItemStack stack) {
        return QuarryModule.Constant.PUMP;
    }
}
