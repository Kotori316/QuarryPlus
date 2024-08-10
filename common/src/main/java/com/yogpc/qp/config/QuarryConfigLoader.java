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
        var quarry = PowerMap.Quarry.CODEC.codec().parse(JavaOps.INSTANCE, config.<Config>get("powerMap.quarry").valueMap()).getOrThrow();
        var powerMap = new PowerMap(quarry);
        var enableMap = EnableMap.from(config.<Config>get("enableMap").valueMap());
        var rebornEnergyConversionCoefficient = config.<Double>get("rebornEnergyConversionCoefficient");

        return new QuarryConfigImpl(debug, powerMap, enableMap, rebornEnergyConversionCoefficient);
    }

    record QuarryConfigImpl(
        boolean debug,
        PowerMap powerMap,
        EnableMap enableMap,
        double rebornEnergyConversionCoefficient
    ) implements QuarryConfig {
    }

    static Pair<ConfigSpec, CommentedConfig> spec(BooleanSupplier inDevelop) {
        Map<String, Object> comments = new HashMap<>();
        var specConfig = CommentedConfig.wrap(comments, InMemoryFormat.withUniversalSupport());
        var config = new ConfigSpec(specConfig);

        config.define("debug", inDevelop.getAsBoolean());
        specConfig.setComment("debug", "In debug mode. Default: " + inDevelop.getAsBoolean());

        defineDouble(config, specConfig, "rebornEnergyConversionCoefficient", 1d / 16d, 0d, 1e10, "[Fabric ONLY] 1E = ?FE");

        // powerMap.quarry.*
        defineInCodec(config, specConfig, "powerMap.quarry", PowerMap.Default.QUARRY);

        // enableMap.*
        defineEnableMap(config, specConfig, "enableMap", EnableMap.getDefault());

        return Pair.of(config, specConfig);
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
            spec.define(key, e.getValue().getAsBoolean());
            commentMap.setComment(key, "%s Default: %b".formatted(e.getKey(), e.getValue().getAsBoolean()));
        }
    }
}
