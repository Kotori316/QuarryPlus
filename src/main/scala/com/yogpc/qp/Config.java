package com.yogpc.qp;

import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.JsonObject;
import com.yogpc.qp.machines.PowerConfig;
import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.machines.advquarry.BlockAdvQuarry;
import com.yogpc.qp.machines.quarry.QuarryBlock;
import com.yogpc.qp.machines.quarry.SFQuarryBlock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

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
        public final ForgeConfigSpec.BooleanValue removeCommonMaterialsByCD;
        public final ForgeConfigSpec.BooleanValue reduceMarkerGuideLineIfPlayerIsFar;
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
            sfqEnergy = builder.comment("The amount of energy[FE] that Solid Fuel Quarry generates in a tick.").defineInRange("sfqEnergy", 2d, 0d, 100d);
            removeCommonMaterialsByCD = builder.comment("Remove common materials(Stone, Dirt, Grass, Sand) obtained by Chunk Destroyer").define("removeCommonMaterialsByCD", true);
            reduceMarkerGuideLineIfPlayerIsFar = builder.comment("Remove MarkerPlus guide line if player is too far from the marker.").define("reduceMarkerGuideLineIfPlayerIsFar", false);
            builder.pop();
        }
    }

    public static class EnableMap {
        private final Map<String, BooleanSupplier> machinesMap;

        public EnableMap(ForgeConfigSpec.Builder builder) {
            builder.comment("QuarryPlus Machines. Set true to enable machine or item.").push("machines");
            var defaultConfig = GsonHelper.parse(new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream("/machine_default.json"), "Content in Jar must not be absent.")
            ));
            machinesMap = defaultConfig.entrySet().stream()
                .map(e -> Map.entry(new ResourceLocation(QuarryPlus.modID, e.getKey()), Holder.EnableOrNot.valueOf(e.getValue().getAsString())))
                .map(e -> new Holder.EntryConditionHolder(e.getKey(), e.getValue()))
                .filter(Holder.EntryConditionHolder::configurable)
                .sorted(Comparator.comparing(Holder.EntryConditionHolder::path))
                .map(n -> Map.entry(n.path(), builder.define(n.path(), !FMLEnvironment.production || n.condition().on())))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> () -> e.getValue().get()));
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

        public boolean enabled(@Nullable ResourceLocation location) {
            if (location == null) return false;
            return enabled(location.getPath());
        }

        public void set(String name, boolean value) {
            this.machinesMap.put(name, () -> value);
        }
    }

    public static class PowerMap {
        @VisibleForTesting
        final Map<String, Map<String, ForgeConfigSpec.DoubleValue>> map;

        private record Key(String machineName, String configName) {
        }

        private record KeyPair(Key key, double value) {
            String machineName() {
                return key().machineName();
            }
        }

        PowerMap(ForgeConfigSpec.Builder builder) {
            map = new HashMap<>();
            builder.comment("Power settings of each machines").push("powers");
            var defaultConfig = GsonHelper.parse(new InputStreamReader(
                Objects.requireNonNull(PowerMap.class.getResourceAsStream("/power_default.json"), "Content in Jar must not be absent.")
            ));
            var valuesFromJson = getKeys(defaultConfig);
            var keys = valuesFromJson.stream().map(KeyPair::key).collect(Collectors.toSet());
            var valuesNotInJson = PowerConfig.getAllMethods()
                .flatMap(m -> Stream.of(QuarryBlock.NAME, SFQuarryBlock.NAME, BlockAdvQuarry.NAME)
                    .filter(name -> !keys.contains(new Key(name, m.getName())))
                    .map(name -> new KeyPair(new Key(name, m.getName()), getDefaultValue(m)))
                );
            Stream.concat(valuesFromJson.stream(), valuesNotInJson)
                .collect(Collectors.groupingBy(KeyPair::machineName))
                .entrySet()
                .stream().map(e -> {
                    var key = e.getKey();
                    builder.push(key);
                    var m = e.getValue().stream().map(keyPair ->
                        Map.entry(keyPair.key().configName(), builder.defineInRange(keyPair.key().configName(), keyPair.value(), 0, 1e9))
                    ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                    builder.pop();
                    return Map.entry(key, m);
                })
                .forEach(e -> map.put(e.getKey(), e.getValue()));
            builder.pop();
        }

        public OptionalDouble get(String machineName, String configName) {
            return Optional.ofNullable(this.map.get(machineName))
                .flatMap(m -> Optional.ofNullable(m.get(configName)))
                .map(ForgeConfigSpec.ConfigValue::get)
                .map(OptionalDouble::of)
                .orElse(OptionalDouble.empty());
        }

        public boolean has(String machineName) {
            return this.map.containsKey(machineName);
        }

        private static double getDefaultValue(Method method) {
            try {
                var value = method.invoke(PowerConfig.DEFAULT);
                if (value instanceof Long aLong) {
                    return aLong.doubleValue() / PowerTile.ONE_FE;
                } else if (value instanceof Double aDouble) {
                    return aDouble;
                } else {
                    throw new IllegalStateException("Non expected value was returned in executing %s. value=%s".formatted(method, value));
                }
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }

        private static List<KeyPair> getKeys(JsonObject object) {
            return object.keySet().stream()
                .flatMap(machineName ->
                    object.getAsJsonObject(machineName).entrySet().stream()
                        .map(e -> new KeyPair(new Key(machineName, e.getKey()), e.getValue().getAsDouble())))
                .toList();
        }
    }
}
