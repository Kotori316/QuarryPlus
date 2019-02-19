package com.yogpc.qp.utils;

import java.util.function.BooleanSupplier;

import com.google.gson.JsonObject;
import com.yogpc.qp.Config;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;

@SuppressWarnings("unused") // accessed via reflection
public class EnableCondition implements IConditionFactory {
    public static final String NAME = "quarryplus:machine_enabled";
    @Override
    public BooleanSupplier parse(JsonContext context, JsonObject json) {
        String s = JsonUtils.getString(json, "value");
        return () -> !Config.content().disableMapJ().getOrDefault(scala.Symbol.apply(s), true);
    }
}
