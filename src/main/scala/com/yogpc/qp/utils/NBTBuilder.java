package com.yogpc.qp.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class NBTBuilder {

    public static <K, V> NBTTagList fromMap(Map<? extends K, ? extends V> map, String keyName, String valueName,
                                            Function<? super K, ? extends NBTBase> keyFunction, Function<? super V, ? extends NBTBase> valueFunction) {
        NBTTagList list = new NBTTagList();
        map.forEach((key, value) -> {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setTag(keyName, keyFunction.apply(key));
            compound.setTag(valueName, valueFunction.apply(value));
            list.appendTag(compound);
        });
        return list;
    }

    public static <K, V> Map<K, V> fromList(NBTTagList list, Function<? super NBTTagCompound, ? extends K> keyFunction, Function<? super NBTTagCompound, ? extends V> valueFunction,
                                            Predicate<? super K> keyFilter, Predicate<? super V> valuePredicate) {
        Map<K, V> map = new HashMap<>(list.tagCount());
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound at = list.getCompoundTagAt(i);
            K key = keyFunction.apply(at);
            V value = valueFunction.apply(at);
            if (keyFilter.test(key) && valuePredicate.test(value))
                map.put(key, value);
        }
        return map;
    }

}
