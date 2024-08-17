package com.yogpc.qp.machine.module;

import com.yogpc.qp.machine.QpItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * This item is always disabled in Fabric.
 */
public final class PumpModuleItem extends QpItem implements QuarryModuleProvider.Item {
    public static final String NAME = "pump_module";

    public PumpModuleItem() {
        super(new Properties(), NAME);
    }

    @Override
    public QuarryModule getModule(@NotNull ItemStack stack) {
        return QuarryModule.Constant.PUMP;
    }
}
