package com.yogpc.qp;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

import com.yogpc.qp.machines.PowerConfig;
import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.machines.advquarry.BlockAdvQuarry;
import com.yogpc.qp.machines.mini_quarry.MiniQuarryBlock;
import com.yogpc.qp.machines.quarry.QuarryBlock;
import com.yogpc.qp.machines.quarry.SFQuarryBlock;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class Config {
    public final Common common;
    public final EnableMap enableMap;
    public final PowerMap powerMap;

    public Config(ForgeConfigSpec.Builder builder) {
        common = new Common(builder);
        enableMap = new EnableMap(builder);
        powerMap = new PowerMap(builder);
    }

    public boolean debug() {
        return !FMLEnvironment.production || common.debug.get();
    }

    public static class Common {
        public final ForgeConfigSpec.IntValue netherTop;
        private final ForgeConfigSpec.BooleanValue debug;
        public final ForgeConfigSpec.BooleanValue noEnergy;
        public final ForgeConfigSpec.BooleanValue convertDeepslateOres;
        public final ForgeConfigSpec.DoubleValue sfqEnergy;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> spawnerBlackList;

        public Common(ForgeConfigSpec.Builder builder) {
            var inDev = !FMLEnvironment.production;
            builder.comment("QuarryPlus Setting").push("common");
            netherTop = builder.comment("The top of Nether").defineInRange("netherTop", inDev ? 128 : 127, -256, 256);
            debug = builder.comment("debug mode").define("debug", inDev);
            noEnergy = builder.comment("no energy").define("noEnergy", false);
            convertDeepslateOres = builder.comment("Whether quarry converts deepslate ore to normal ore.").define("convertDeepslateOres", false);
            var disabledEntities = List.of("minecraft:ender_dragon", "minecraft:wither",
                "minecraft:area_effect_cloud", "minecraft:item", "minecraft:player");
            spawnerBlackList = builder.comment("Spawner Controller Blacklist").defineListAllowEmpty(List.of("spawnerBlacklist"), () -> disabledEntities, s -> s instanceof String);
            sfqEnergy = builder.comment("The amount of energy that Solid Fuel Quarry generates in a tick.").defineInRange("sfqEnergy", 2d, 0d, 100d);
            builder.pop();
        }
    }

    public static class EnableMap {
        private final Map<String, BooleanSupplier> machinesMap;

        public EnableMap(ForgeConfigSpec.Builder builder) {
            builder.comment("QuarryPlus Machines. Set true to enable machine or item.").push("machines");
            machinesMap = Holder.conditionHolders().stream()
                .filter(Holder.EntryConditionHolder::configurable)
                .sorted(Comparator.comparing(Holder.EntryConditionHolder::path))
                .map(n -> Map.entry(n.path(), builder.define(n.path(), !FMLEnvironment.production || n.condition().on())))
                .map(e -> Map.entry(e.getKey(), (BooleanSupplier) (() -> e.getValue().get())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            builder.pop();
        }

        public boolean enabled(String s) {
            return Optional.ofNullable(machinesMap.get(s))
                .map(BooleanSupplier::getAsBoolean)
                .or(() -> Holder.conditionHolders().stream()
                    .filter(h -> h.path().equals(s))
                    .findFirst()
                    .map(Holder.EntryConditionHolder::condition)
                    .map(Holder.EnableOrNot::on))
                .orElse(Boolean.FALSE);
        }

        public boolean enabled(ResourceLocation location) {
            return enabled(location.getPath());
        }

        public void set(String name, boolean value) {
            this.machinesMap.put(name, () -> value);
        }
    }

    public static class PowerMap {
        private final Map<String, Map<String, ForgeConfigSpec.DoubleValue>> map;

        PowerMap(ForgeConfigSpec.Builder builder) {
            map = new HashMap<>();
            builder.comment("Power settings of each machines").push("powers");
            var machines = List.of(QuarryBlock.NAME, SFQuarryBlock.NAME, BlockAdvQuarry.NAME);
            for (var name : machines) {
                builder.push(name);
                var values =
                    Arrays.stream(PowerConfig.class.getMethods())
                        .filter(m -> Character.isLowerCase(m.getName().charAt(0)))
                        .filter(m -> m.getReturnType() == Long.TYPE || m.getReturnType() == Double.TYPE)
                        .map(PowerMap::getDefaultValue)
                        .map(e -> Map.entry(e.getKey(), builder.defineInRange(e.getKey(), e.getValue(), 0d, 1e9)))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                map.put(name, values);
                builder.pop();
            }
            {// Mini Quarry
                Map<String, ForgeConfigSpec.DoubleValue> mMap = new HashMap<>();
                builder.push(MiniQuarryBlock.NAME);
                mMap.put("breakBlockBase", builder.comment("Energy required to remove one block.").defineInRange("breakBlockBase", 20d, 0, 1e9));
                builder.pop();
                map.put(MiniQuarryBlock.NAME, mMap);
            }

            builder.pop();
        }

        public OptionalDouble get(String machineName, String configName) {
            return Optional.ofNullable(this.map.get(machineName))
                .flatMap(m -> Optional.ofNullable(m.get(configName)))
                .map(ForgeConfigSpec.ConfigValue::get)
                .map(OptionalDouble::of)
                .orElse(OptionalDouble.empty());
        }

        private static Map.Entry<String, Double> getDefaultValue(Method method) {
            var name = method.getName();
            try {
                var value = method.invoke(PowerConfig.DEFAULT);
                if (value instanceof Long aLong) {
                    return Map.entry(name, aLong.doubleValue() / PowerTile.ONE_FE);
                } else if (value instanceof Double aDouble) {
                    return Map.entry(name, aDouble);
                } else {
                    throw new IllegalStateException("Non expected value was returned in executing %s. value=%s".formatted(method, value));
                }
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
