package com.yogpc.qp.machines.workbench;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.conditions.ICondition;

public final class QuarryDebugCondition implements ICondition {
    public static final ResourceLocation NAME = new ResourceLocation("quarryplus:debug_enabled");

    public QuarryDebugCondition() {
    }

    @Override
    public boolean test(IContext context) {
        return !FMLEnvironment.production;
    }

    public static final Codec<QuarryDebugCondition> CODEC = Codec.unit(QuarryDebugCondition::new).stable();

    @Override
    public Codec<? extends ICondition> codec() {
        return CODEC;
    }

}
