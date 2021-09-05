package com.yogpc.qp.machines.workbench;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class QuarryDebugCondition implements ICondition {
    public static final ResourceLocation NAME = new ResourceLocation("quarryplus:debug_enabled");

    public QuarryDebugCondition() {
    }

    @Override
    public ResourceLocation getID() {
        return NAME;
    }

    @Override
    public boolean test() {
        return !FMLEnvironment.production;
    }

    public static class Serializer implements IConditionSerializer<QuarryDebugCondition> {

        @Override
        public void write(JsonObject json, QuarryDebugCondition condition) {
        }

        @Override
        public QuarryDebugCondition read(JsonObject json) {
            return new QuarryDebugCondition();
        }

        @Override
        public ResourceLocation getID() {
            return QuarryDebugCondition.NAME;
        }
    }
}
