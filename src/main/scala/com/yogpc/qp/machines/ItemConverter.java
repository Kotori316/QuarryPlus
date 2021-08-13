package com.yogpc.qp.machines;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import net.minecraft.item.ItemStack;

public record ItemConverter(List<Map.Entry<Predicate<MachineStorage.ItemKey>, MachineStorage.ItemKey>> conversionMap) {

    public ItemConverter(Map<MachineStorage.ItemKey, MachineStorage.ItemKey> map) {
        this(map.entrySet().stream().map(e -> Map.entry(Predicate.<MachineStorage.ItemKey>isEqual(e.getKey()), e.getValue())).toList());
    }

    public ItemStack map(ItemStack before) {
        if (conversionMap().isEmpty()) return before;
        var key = new MachineStorage.ItemKey(before);
        return conversionMap()
            .stream()
            .filter(e -> e.getKey().test(key))
            .findFirst()
            .map(Map.Entry::getValue)
            .map(k -> k.toStack(before.getCount()))
            .orElse(before);
    }
}
