package com.yogpc.qp.machines.workbench;

import com.google.gson.JsonObject;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

public final class EnableCondition implements ICondition {
    public static final ResourceLocation NAME = new ResourceLocation("quarryplus:machine_enabled");
    private final String machineName;

    public EnableCondition(String value) {
        this.machineName = value;
    }

    @Override
    public ResourceLocation getID() {
        return NAME;
    }

    @Override
    @SuppressWarnings("removal")
    public boolean test() {
        return this.test(IContext.EMPTY);
    }

    @Override
    public boolean test(IContext context) {
        if (QuarryPlus.config == null) {
            return true; // Ignore in tests.
        } else {
            return QuarryPlus.config.enableMap.enabled(machineName);
        }
    }

    public static class Serializer implements IConditionSerializer<EnableCondition> {

        @Override
        public void write(JsonObject json, EnableCondition condition) {
            json.addProperty("value", condition.machineName);
        }

        @Override
        public EnableCondition read(JsonObject json) {
            return new EnableCondition(GsonHelper.getAsString(json, "value"));
        }

        @Override
        public ResourceLocation getID() {
            return EnableCondition.NAME;
        }
    }
}
