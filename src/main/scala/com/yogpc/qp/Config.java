package com.yogpc.qp;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class Config {
    public final Common common;
    public final EnableMap enableMap;

    public Config(ForgeConfigSpec.Builder builder) {
        common = new Common(builder);
        enableMap = new EnableMap(builder);
    }

    public boolean debug() {
        return !FMLEnvironment.production || common.debug.get();
    }

    public static class Common {
        public final ForgeConfigSpec.IntValue netherTop;
        private final ForgeConfigSpec.BooleanValue debug;
        public final ForgeConfigSpec.BooleanValue noEnergy;
        public final ForgeConfigSpec.BooleanValue convertDeepslateOres;
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
            builder.pop();
        }
    }

    public static class EnableMap {
        private final Map<String, ForgeConfigSpec.BooleanValue> machinesMap;

        public EnableMap(ForgeConfigSpec.Builder builder) {
            builder.comment("QuarryPlus Machines. Set true to enable machine or item.").push("machines");
            machinesMap = Holder.conditionHolders().stream()
                .filter(Holder.EntryConditionHolder::configurable)
                .map(n -> Map.entry(n.path(), builder.define(n.path(), !FMLEnvironment.production || n.condition().on())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            builder.pop();
        }

        public boolean enabled(String s) {
            return Optional.ofNullable(machinesMap.get(s))
                .map(ForgeConfigSpec.ConfigValue::get)
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
    }
}
