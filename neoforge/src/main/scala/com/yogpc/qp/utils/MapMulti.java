package com.yogpc.qp.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;

public class MapMulti {
    public static <FROM, TO extends FROM> BiConsumer<FROM, Consumer<TO>> cast(Class<TO> toClass) {
        Objects.requireNonNull(toClass);
        return (from, toConsumer) -> {
            if (toClass.isInstance(from)) {
                toConsumer.accept(toClass.cast(from));
            }
        };
    }

    public static <FROM, TO extends FROM> Function<FROM, Optional<TO>> optCast(Class<TO> toClass) {
        return from -> toClass.isInstance(from) ? Optional.of(toClass.cast(from)) : Optional.empty();
    }

    public static <TO, OTHER> BiConsumer<String, Consumer<Pair<TO, OTHER>>>
    getEntry(Registry<TO> registry, Function<String, OTHER> keyToOther) {
        return (s, toConsumer) -> {
            ResourceLocation key = new ResourceLocation(s);
            if (registry.containsKey(key)) {
                toConsumer.accept(Pair.of(registry.get(key), keyToOther.apply(s)));
            }
        };
    }

    public static <T extends JsonElement> Collector<T, ?, JsonArray> jsonArrayCollector() {
        return Collector.of(
            JsonArray::new, JsonArray::add, (jsonArray, jsonArray2) -> {
                jsonArray.addAll(jsonArray2);
                return jsonArray;
            }, Collector.Characteristics.IDENTITY_FINISH
        );
    }
}
