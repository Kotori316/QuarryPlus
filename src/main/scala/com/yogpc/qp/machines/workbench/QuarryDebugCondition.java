package com.yogpc.qp.machines.workbench;

import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.fml.loading.FMLEnvironment;

public final class QuarryDebugCondition implements ICondition {
    public static final ResourceLocation NAME = new ResourceLocation("quarryplus:debug_enabled");

    public QuarryDebugCondition() {
    }

    @Override
    public boolean test(IContext context) {
        return !FMLEnvironment.production;
    }

    public static final Codec<QuarryDebugCondition> CODEC = Codec.EMPTY.codec().comapFlatMap(
        v -> DataResult.success(new QuarryDebugCondition()),
        v -> Unit.INSTANCE
    );

    @Override
    public Codec<? extends ICondition> codec() {
        return CODEC;
    }

}
