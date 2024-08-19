package com.yogpc.qp.machine.exp;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.module.QuarryModule;
import net.minecraft.resources.ResourceLocation;

public interface ExpModule extends QuarryModule {
    void addExp(int amount);

    int getExp();

    @Override
    default ResourceLocation moduleId() {
        return ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, "exp_module");
    }
}
