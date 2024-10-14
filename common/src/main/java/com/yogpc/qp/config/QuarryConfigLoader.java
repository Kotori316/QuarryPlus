package com.yogpc.qp.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.InMemoryFormat;
import com.mojang.serialization.JavaOps;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.PowerMap;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.RecordComponent;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

final class QuarryConfigLoader {
    static QuarryConfig load(Config config) {
        return load(config, () -> false);
    }

    static QuarryConfig load(Config config, BooleanSupplier inDevelop) {
        var specPair = spec(inDevelop);
        specPair.getKey().correct(config, (action, path, incorrectValue, correctedValue) ->
            QuarryPlus.LOGGER.debug("Config corrected '{}': {} -> {}", path, incorrectValue, correctedValue)
        );
        if (config instanceof CommentedConfig c) {
            c.putAllComments(specPair.getValue());
        }

        var debug = config.<Boolean>get("debug");
        var noEnergy = config.<Boolean>get("noEnergy");
        var quarry = PowerMap.Quarry.CODEC.codec().parse(JavaOps.INSTANCE, config.<Config>get("powerMap.quarry").valueMap()).getOrThrow();
        var advQuarry = PowerMap.AdvQuarry.CODEC.codec().parse(JavaOps.INSTANCE, config.<Config>get("powerMap.advQuarry").valueMap()).getOrThrow();
        var powerMap = new PowerMap(quarry, advQuarry);
        var enableMapConfig = config.<Config>get("enableMap");
        EnableMap enableMap;
        if (enableMapConfig != null) {
            enableMap = EnableMap.from(enableMapConfig.valueMap());
        } else {
            enableMap = new EnableMap();
        }
        var rebornEnergyConversionCoefficient = config.<Double>get("rebornEnergyConversionCoefficient");
        var removeBedrockOnNetherTop = config.<Boolean>get("removeBedrockOnNetherTop");
        var enableChunkLoader = config.<Boolean>get("enableChunkLoader");
        var convertDeepslateOres = config.<Boolean>get("convertDeepslateOres");
        var removeCommonMaterialsByChunkDestroyer = config.<Boolean>get("removeCommonMaterialsByChunkDestroyer");

        return new QuarryConfigImpl(debug, noEnergy, powerMap, enableMap, rebornEnergyConversionCoefficient, removeBedrockOnNetherTop, enableChunkLoader, convertDeepslateOres, removeCommonMaterialsByChunkDestroyer);
    }

    record QuarryConfigImpl(
        boolean debug,
        boolean noEnergy,
        PowerMap powerMap,
        EnableMap enableMap,
        double rebornEnergyConversionCoefficient,
        boolean removeBedrockOnNetherTop,
        boolean enableChunkLoader,
        boolean convertDeepslateOres,
        boolean removeCommonMaterialsByChunkDestroyer
    ) implements QuarryConfig {
    }

    static Pair<ConfigSpec, CommentedConfig> spec(BooleanSupplier inDevelop) {
        Map<String, Object> comments = new HashMap<>();
        var specConfig = CommentedConfig.wrap(comments, InMemoryFormat.withUniversalSupport());
        var config = new ConfigSpec(specConfig);

        defineBoolean(config, specConfig, "debug", inDevelop.getAsBoolean(), "In debug mode");
        defineBoolean(config, specConfig, "noEnergy", false, "Enable No Energy mode");

        defineDouble(config, specConfig, "rebornEnergyConversionCoefficient", 1d / 16d, 0d, 1e10, "[Fabric ONLY] 1E = ?FE");
        defineBoolean(config, specConfig, "removeBedrockOnNetherTop", inDevelop.getAsBoolean(), "Remove bedrock at y=127 in Nether");
        defineBoolean(config, specConfig, "enableChunkLoader", true, "Enable Chunk Loader in machines");
        defineBoolean(config, specConfig, "convertDeepslateOres", false, "Convert Deepslate ores to normal ores");
        defineBoolean(config, specConfig, "removeCommonMaterialsByChunkDestroyer", true, "Remove common materials(Base blocks in Over world and the Nether) obtained by Chunk Destroyer");

        // powerMap.quarry.*
        defineInCodec(config, specConfig, "powerMap.quarry", PowerMap.Default.QUARRY);
        // powerMap.advQuarry.*
        defineInCodec(config, specConfig, "powerMap.advQuarry", PowerMap.Default.ADV_QUARRY);

        // enableMap.*
        defineEnableMap(config, specConfig, "enableMap", EnableMap.getDefault(inDevelop));

        return Pair.of(config, specConfig);
    }

    static void defineBoolean(ConfigSpec spec, CommentedConfig commentMap, String key, boolean defaultValue, String comment) {
        spec.define(key, defaultValue);
        commentMap.setComment(key, "%s. Default: %s".formatted(comment, defaultValue));
    }

    static void defineDouble(ConfigSpec spec, CommentedConfig commentMap, String key, double defaultValue, double min, double max, String comment) {
        spec.defineInRange(key, defaultValue, min, max);
        commentMap.setComment(key, "%s. Default: %f, Min: %.1f, Max: %.1f".formatted(comment, defaultValue, min, max));
    }

    static <T extends Record> void defineInCodec(ConfigSpec spec, CommentedConfig commentMap, String prefix, T instance) {
        var clazz = instance.getClass();
        if (!clazz.isRecord()) {
            throw new IllegalArgumentException("Instance must be a record, but is " + instance);
        }
        var fields = instance.getClass().getRecordComponents();
        try {
            for (RecordComponent field : fields) {
                var accessor = field.getAccessor();
                var defaultValue = accessor.invoke(instance);
                var key = prefix + "." + field.getName();
                if (field.getType().equals(Double.TYPE)) {
                    defineDouble(spec, commentMap, key, (Double) defaultValue, 0d, 1e10, field.getName());
                } else {
                    spec.define(key, defaultValue);
                    commentMap.setComment(key, "%s. Default: %s".formatted(field.getName(), defaultValue));
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    static void defineEnableMap(ConfigSpec spec, CommentedConfig commentMap, String prefix, EnableMap enableMap) {
        var map = enableMap.getMachinesMap();
        for (var e : map.entrySet()) {
            var key = prefix + "." + e.getKey();
            spec.define(key, e.getValue());
            var defaultSetting = EnableMap.getDefaultValue(e.getKey());
            if (defaultSetting == EnableMap.EnableOrNot.ALWAYS_OFF) {
                commentMap.setComment(key, "This item can't be enabled in this platform. Configuration will be ignored. (%s)".formatted(e.getKey()));
            } else {
                commentMap.setComment(key, "%s Default: %b".formatted(e.getKey(), e.getValue()));
            }
        }
    }
}
