package com.yogpc.qp.utils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collector;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;

public class NBTBuilder<T extends INBT> {

    private Map<String, T> map = new LinkedHashMap<>();

    public NBTBuilder<T> setTag(Map.Entry<String, T> entry) {
        return setTag(entry.getKey(), entry.getValue());
    }

    public NBTBuilder<T> setTag(String key, T value) {
        map.put(key, value);
        return this;
    }

    public static <T extends INBT> NBTBuilder<T> appendAll(NBTBuilder<T> b1, NBTBuilder<T> b2) {
        b1.map.putAll(b2.map);
        return b1;
    }

    public static <K extends INBT, T extends Map.Entry<String, K>> Collector<T, NBTBuilder<K>, CompoundNBT> toNBTTag() {
        return Collector.of(NBTBuilder::new, NBTBuilder::setTag,
            NBTBuilder::appendAll, NBTBuilder::toTag,
            Collector.Characteristics.UNORDERED);
    }

    public CompoundNBT toTag() {
        CompoundNBT tag = new CompoundNBT();
        map.forEach(tag::put);
        return tag;
    }

}
