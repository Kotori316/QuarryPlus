package com.yogpc.qp;

import com.google.gson.JsonObject;
import com.yogpc.qp.machines.PowerConfig;
import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.machines.advpump.BlockAdvPump;
import com.yogpc.qp.machines.advquarry.BlockAdvQuarry;
import com.yogpc.qp.machines.marker.TileMarker;
import com.yogpc.qp.machines.mini_quarry.MiniQuarryBlock;
import com.yogpc.qp.machines.quarry.QuarryBlock;
import com.yogpc.qp.machines.quarry.SFQuarryBlock;
import com.yogpc.qp.utils.MapStreamSyntax;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.yogpc.qp.utils.MapStreamSyntax.toAny;
import static com.yogpc.qp.utils.MapStreamSyntax.toEntry;

public class Config {
    public final Common common;
    public final EnableMap enableMap;
    public final PowerMap powerMap;
    public final AcceptableEnchantmentsMap acceptableEnchantmentsMap;

    public Config(ModConfigSpec.Builder builder) {
        common = new Common(builder);
        enableMap = new EnableMap(builder);
        powerMap = new PowerMap(builder);
        acceptableEnchantmentsMap = new AcceptableEnchantmentsMap(builder);
    }

    public boolean debug() {
        return !FMLEnvironment.production || common.debug.get();
    }

    public Map<String, Map<String, ?>> getAll() {
        return Map.of(
            "common", common.getAll(),
            "enableMap", enableMap.getAll(),
            "powerMap", powerMap.getAll(),
            "acceptableEnchantmentsMap", acceptableEnchantmentsMap.getAll()
        );
    }

    public static class Common {
        public final ModConfigSpec.IntValue netherTop;
        private final ModConfigSpec.BooleanValue debug;
        public final ModConfigSpec.BooleanValue noEnergy;
        public final ModConfigSpec.BooleanValue convertDeepslateOres;
        public final ModConfigSpec.DoubleValue sfqEnergy;
        public final ModConfigSpec.BooleanValue removeCommonMaterialsByCD;
        public final ModConfigSpec.BooleanValue reduceMarkerGuideLineIfPlayerIsFar;
        public final ModConfigSpec.BooleanValue removeFrameAfterQuarryIsRemoved;
        public final ModConfigSpec.BooleanValue allowWorkInClaimedChunkByFBTChunks;
        public final ModConfigSpec.ConfigValue<List<? extends String>> spawnerBlackList;
        public final ModConfigSpec.IntValue chunkDestroyerLimit;
        public final ModConfigSpec.IntValue flexMarkerMaxDistance;
        public final ModConfigSpec.BooleanValue allowWorkbenchExtraction;
        public final ModConfigSpec.BooleanValue enableChunkLoader;
        public final ModConfigSpec.BooleanValue logAllQuarryWork;

        public Common(ModConfigSpec.Builder builder) {
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
            removeCommonMaterialsByCD = builder.comment("Remove common materials(Stone, Dirt, Grass, Sand, etc.) obtained by Chunk Destroyer").define("removeCommonMaterialsByCD", true);
            reduceMarkerGuideLineIfPlayerIsFar = builder.comment("Remove MarkerPlus guide line if player is too far from the marker.").define("reduceMarkerGuideLineIfPlayerIsFar", false);
            removeFrameAfterQuarryIsRemoved = builder.comment("Remove adjacent frames when quarry is removed.").define("removeFrameAfterQuarryIsRemoved", false);
            allowWorkInClaimedChunkByFBTChunks = builder.comment("Allow quarries to work in claimed chunk(FTB Chunks).").define("allowWorkInClaimedChunkByFBTChunks", false);
            chunkDestroyerLimit = builder.comment("The range limit(unit: blocks) of ChunkDestroyer. Set -1 or 0 to remove limitation.")
                .defineInRange("chunkDestroyerLimit", -1, -1, Integer.MAX_VALUE);
            flexMarkerMaxDistance = builder.comment("The max distance(unit: blocks) Flexible Marker can reach")
                .defineInRange("flexMarkerMaxDistance", TileMarker.MAX_SEARCH, 16, 1 << 12);
            allowWorkbenchExtraction = builder.comment("True to allow pipes to extract items in WorkbenchPlus").define("allowWorkbenchExtraction", false);
            enableChunkLoader = builder.comment("Use simple chunk load function.", "If you have other chunk load system, please disable this and use other mods.")
                .define("enableChunkLoader", true);
            logAllQuarryWork = builder.comment("Trace quarry work").define("logAllQuarryWork", inDev);
            builder.pop();
        }

        @VisibleForTesting
        Map<String, Object> getAll() {
            return getAllInClass(this);
        }
    }

    public static class EnableMap {
        private final Map<String, BooleanSupplier> machinesMap;

        public EnableMap(ModConfigSpec.Builder builder) {
            builder.comment("QuarryPlus Machines. Set true to enable machine or item.").push("machines");
            var defaultConfig = GsonHelper.parse(new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream("/machine_default.json"), "Content in Jar must not be absent.")
            ));
            machinesMap = defaultConfig.entrySet().stream()
                .map(toEntry(e -> new ResourceLocation(QuarryPlus.modID, e.getKey()), e -> Holder.EnableOrNot.valueOf(e.getValue().getAsString())))
                .map(toAny(Holder.EntryConditionHolder::new))
                .filter(Holder.EntryConditionHolder::configurable)
                .sorted(Comparator.comparing(Holder.EntryConditionHolder::path))
                .map(toEntry(Holder.EntryConditionHolder::path, n -> builder.define(n.path(), !FMLEnvironment.production || n.condition().on())))
                .collect(Collectors.toMap(Map.Entry::getKey, MapStreamSyntax.valueToAny(v -> v::get)));
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

        @VisibleForTesting
        Map<String, Boolean> getAll() {
            return machinesMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getAsBoolean()));
        }
    }

    public static class PowerMap {
        @VisibleForTesting
        final Map<String, Map<String, ModConfigSpec.DoubleValue>> map;
        public final ModConfigSpec.LongValue ic2ConversionRate;

        private record Key(String machineName, String configName) {
        }

        private record KeyPair(Key key, double value) {
            String machineName() {
                return key().machineName();
            }
        }

        PowerMap(ModConfigSpec.Builder builder) {
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
            builder.comment("IC2 integration").push("ic2-integration");
            // Default: 1 EU = 4 FE = 4,000,000,000 nano FE
            ic2ConversionRate = builder.comment("The rate to convert EU to nano FE. Default(4,000,000,000) is the rate of 1 EU = 4 FE")
                .defineInRange("conversionRate", 4 * PowerTile.ONE_FE, 1L, Long.MAX_VALUE);
            builder.pop();
        }

        public OptionalDouble get(String machineName, String configName) {
            return Optional.ofNullable(this.map.get(machineName))
                .flatMap(m -> Optional.ofNullable(m.get(configName)))
                .map(ModConfigSpec.ConfigValue::get)
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
                        .map(toAny((configName, power) -> new KeyPair(new Key(machineName, configName), power.getAsDouble())))
                )
                .toList();
        }

        @VisibleForTesting
        Map<String, Map<String, Double>> getAll() {
            return map.entrySet().stream()
                .map(MapStreamSyntax.values(m ->
                    m.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()))
                )).collect(MapStreamSyntax.entryToMap());
        }
    }

    public static class AcceptableEnchantmentsMap {
        @VisibleForTesting
        final Map<String, ModConfigSpec.ConfigValue<List<? extends String>>> enchantmentsMap;

        public AcceptableEnchantmentsMap(ModConfigSpec.Builder builder) {
            builder.comment("Enchantments. Defines enchantments machines can accept.").push("enchantments");
            var targets = List.of(
                Map.entry(new ResourceLocation(QuarryPlus.modID, QuarryBlock.NAME), vanillaAllEnchantments()),
                Map.entry(new ResourceLocation(QuarryPlus.modID, BlockAdvQuarry.NAME), vanillaAllEnchantments()),
                // Map.entry(new ResourceLocation(QuarryPlus.modID, MiningWellBlock.NAME), List.of("minecraft:efficiency")), // Not configurable
                Map.entry(new ResourceLocation(QuarryPlus.modID, MiniQuarryBlock.NAME), miniQuarryEnchantments()),
                Map.entry(new ResourceLocation(QuarryPlus.modID, BlockAdvPump.NAME), pumpEnchantments())
            );

            enchantmentsMap = targets.stream()
                .map(e -> Map.entry(e.getKey().getPath(), builder.defineListAllowEmpty(List.of(e.getKey().getPath()), e::getValue,
                    o -> o instanceof String s && ResourceLocation.isValidResourceLocation(s))))
                .collect(MapStreamSyntax.entryToMap());
        }

        @NotNull
        @VisibleForTesting
        static List<String> vanillaAllEnchantments() {
            return List.of("minecraft:efficiency", "minecraft:unbreaking", "minecraft:fortune", "minecraft:silk_touch");
        }

        @NotNull
        @VisibleForTesting
        static List<String> miniQuarryEnchantments() {
            return List.of("minecraft:efficiency", "minecraft:unbreaking");
        }

        @NotNull
        @VisibleForTesting
        static List<String> pumpEnchantments() {
            return List.of("minecraft:efficiency", "minecraft:unbreaking", "minecraft:fortune");
        }

        public Set<Enchantment> getAllowedEnchantments(ResourceLocation machineName) {
            if (machineName == null) return Set.of();
            return Optional.ofNullable(this.enchantmentsMap.get(machineName.getPath()))
                .map(ModConfigSpec.ConfigValue::get)
                .orElseGet(List::of)
                .stream()
                .map(ResourceLocation::new)
                .filter(ForgeRegistries.ENCHANTMENTS::containsKey)
                .map(ForgeRegistries.ENCHANTMENTS::getValue)
                .collect(Collectors.toSet());
        }

        @VisibleForTesting
        Map<String, List<? extends String>> getAll() {
            return enchantmentsMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));
        }
    }

    static <T> Map<String, Object> getAllInClass(T instance) {
        return Stream.of(instance.getClass().getDeclaredFields())
            .filter(f -> ModConfigSpec.ConfigValue.class.isAssignableFrom(f.getType()))
            .map(f -> {
                try {
                    f.setAccessible(true);
                    var config = (ModConfigSpec.ConfigValue<?>) f.get(instance);
                    var value = config.get();
                    return Map.entry(f.getName(), value);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }
            })
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
