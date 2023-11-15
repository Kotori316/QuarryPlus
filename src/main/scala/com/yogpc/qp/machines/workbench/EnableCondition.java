package com.yogpc.qp.machines.workbench;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ICondition;

public final class EnableCondition implements ICondition {
    public static final ResourceLocation NAME = new ResourceLocation("quarryplus:machine_enabled");
    private final String machineName;

    public EnableCondition(String value) {
        this.machineName = value;
    }

    @Override
    public boolean test(IContext context) {
        if (QuarryPlus.config == null) {
            return true; // Ignore in tests.
        } else {
            return QuarryPlus.config.enableMap.enabled(machineName);
        }
    }

    public static final Codec<EnableCondition> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.fieldOf("value").forGetter(enableCondition -> enableCondition.machineName)
        ).apply(instance, EnableCondition::new));

    @Override
    public Codec<? extends ICondition> codec() {
        return CODEC;
    }

}
