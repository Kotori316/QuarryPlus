package com.yogpc.qp.utils;

import com.google.gson.JsonObject;
import com.yogpc.qp.Config;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import scala.Symbol;

public class EnableCondition implements ICondition {
    public static final ResourceLocation NAME = new ResourceLocation("quarryplus:machine_enabled");
    private final Symbol value;

    public EnableCondition(Symbol value) {
        this.value = value;
    }

    @Override
    public ResourceLocation getID() {
        return NAME;
    }

    @Override
    public boolean test() {
        return !Config.common().disabled().get(value).forall(ForgeConfigSpec.BooleanValue::get);
    }

    public static class Serializer implements IConditionSerializer<EnableCondition> {

        @Override
        public void write(JsonObject json, EnableCondition condition) {
            String string = condition.value.name();
            json.addProperty("value", string);
        }

        @Override
        public EnableCondition read(JsonObject json) {
            return new EnableCondition(Symbol.apply(JSONUtils.getString(json, "value")));
        }

        @Override
        public ResourceLocation getID() {
            return EnableCondition.NAME;
        }
    }
}
