package com.yogpc.qp.machines.module;

import net.minecraft.world.item.ItemStack;

public interface QuarryModuleProvider {

    interface Block extends QuarryModuleProvider {
        QuarryModule getModule();
    }

    interface Item extends QuarryModuleProvider {
        QuarryModule getModule(ItemStack stack);
    }
}
