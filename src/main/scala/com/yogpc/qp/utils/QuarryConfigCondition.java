package com.yogpc.qp.utils;

import com.google.gson.JsonObject;
import com.yogpc.qp.Config;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

public class QuarryConfigCondition implements ICondition {
    public static final ResourceLocation LOCATION = new ResourceLocation(QuarryPlus.modID, "is_config_true");
    private final String key;

    public QuarryConfigCondition(String key) {
        this.key = key;
    }

    @Override
    public ResourceLocation getID() {
        return LOCATION;
    }

    @Override
    public boolean test() {
        return Config.common().isOptionTrue(key);
    }

    public static class Serializer implements IConditionSerializer<QuarryConfigCondition> {

        @Override
        public void write(JsonObject json, QuarryConfigCondition value) {
            json.addProperty("key", value.key);
        }

        @Override
        public QuarryConfigCondition read(JsonObject json) {
            String key = JSONUtils.getString(json, "key");
            return new QuarryConfigCondition(key);
        }

        @Override
        public ResourceLocation getID() {
            return QuarryConfigCondition.LOCATION;
        }
    }
}
