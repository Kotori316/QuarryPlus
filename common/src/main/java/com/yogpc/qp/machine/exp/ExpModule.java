package com.yogpc.qp.machine.exp;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.module.QuarryModule;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Optional;

public interface ExpModule extends QuarryModule {
    void addExp(int amount);

    int getExp();

    @Override
    default ResourceLocation moduleId() {
        return ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, "exp_module");
    }

    static Optional<ExpModule> getModule(Collection<QuarryModule> modules) {
        return modules.stream()
            .filter(ExpModule.class::isInstance)
            .map(ExpModule.class::cast)
            .findAny();
    }
}
