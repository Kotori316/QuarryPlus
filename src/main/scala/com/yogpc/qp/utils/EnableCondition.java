package com.yogpc.qp.utils;

import java.util.function.BooleanSupplier;

import com.google.gson.JsonObject;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.IConditionSerializer;

@SuppressWarnings("unused") // accessed via reflection
public class EnableCondition implements IConditionSerializer {
    public static final String NAME = "quarryplus:machine_enabled";

    @Override
    public BooleanSupplier parse(JsonObject json) {
        String s = JsonUtils.getString(json, "value");
        return () -> true;//!Config.content().disableMapJ().getOrDefault(scala.Symbol.apply(s), true);
    }
}
