package com.yogpc.qp.utils;

import java.util.function.BooleanSupplier;

import com.google.gson.JsonObject;
import com.yogpc.qp.Config;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.crafting.IConditionSerializer;
import scala.Symbol;

@SuppressWarnings("unused") // accessed via reflection
public class EnableCondition implements IConditionSerializer {
    public static final String NAME = "quarryplus:machine_enabled";

    @Override
    public BooleanSupplier parse(JsonObject json) {
        String s = JsonUtils.getString(json, "value");
        return () -> !Config.common().disabled().get(Symbol.apply(s)).map(ForgeConfigSpec.BooleanValue::get).getOrElse(() -> Boolean.FALSE);
    }
}
