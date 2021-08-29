package com.yogpc.qp.machines;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.tuple.Pair;

public record ItemConverter(
    List<Map.Entry<Predicate<ItemKey>, Function<ItemKey, ItemKey>>> conversionMap) {

    public ItemConverter(Map<ItemKey, ItemKey> map) {
        this(map.entrySet().stream().map(e -> {
            Predicate<ItemKey> predicate = Predicate.isEqual(e.getKey());
            Function<ItemKey, ItemKey> converter = i -> e.getValue();
            return Map.entry(predicate, converter);
        }).toList());
    }

    public ItemStack map(ItemStack before) {
        if (conversionMap().isEmpty()) return before;
        var key = new ItemKey(before);
        return conversionMap()
            .stream()
            .filter(e -> e.getKey().test(key))
            .findFirst()
            .map(Map.Entry::getValue)
            .map(f -> f.apply(key))
            .map(k -> k.toStack(before.getCount()))
            .orElse(before);
    }

    public static ItemConverter defaultConverter() {
        if (QuarryPlus.config.common.convertDeepslateOres) {
            return deepslateConverter();
        } else {
            return new ItemConverter(List.of());
        }
    }

    public static ItemConverter deepslateConverter() {
        Predicate<ItemKey> predicate = i -> {
            var path = i.getId().getPath();
            return path.contains("deepslate") && path.contains("ore");
        };
        Function<ItemKey, ItemKey> function = i -> {
            var newPath = i.getId().getPath().replace("deepslate_", "").replace("_deepslate", "");
            var id = new Identifier(i.getId().getNamespace(), newPath);
            return Registry.ITEM.getOrEmpty(id).map(item -> new ItemKey(item, i.nbt())).orElse(null);
        };
        return new ItemConverter(List.of(Pair.of(predicate, function)));
    }
}
