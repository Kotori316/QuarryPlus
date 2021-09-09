package com.yogpc.qp.machines.module;

import com.yogpc.qp.Holder;
import net.minecraft.resources.ResourceLocation;

public abstract class ExpModule implements QuarryModule {
    public abstract void addExp(int amount);

    public abstract int getExp();

    @Override
    public ResourceLocation moduleId() {
        return Holder.ITEM_EXP_MODULE.getRegistryName();
    }

    @Override
    public String toString() {
        return ExpModule.class.getSimpleName() + "[exp=" + getExp() + "]";
    }
}
