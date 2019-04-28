package com.yogpc.qp.utils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.INBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.state.IProperty;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import static com.google.common.collect.Streams.stream;
import static jp.t2v.lab.syntax.MapStreamSyntax.*;

public class NBTBuilder<T extends INBTBase> {

    @SuppressWarnings("RedundantCast")
    public static <K, V> NBTTagList fromMap(Map<? extends K, ? extends V> map, String keyName, String valueName,
                                            Function<? super K, ? extends INBTBase> keyFunction, Function<? super V, ? extends INBTBase> valueFunction) {
        return map.entrySet().stream()
            .map(toEntry(keyFunction.compose(Map.Entry::getKey), valueFunction.compose(Map.Entry::getValue)))
            .map(toAny((k, v) -> {
                NBTTagCompound compound = new NBTTagCompound();
                compound.setTag(keyName, ((INBTBase) k));
                compound.setTag(valueName, ((INBTBase) v));
                return compound;
            }))
            .collect(Collectors.toCollection(NBTTagList::new));
    }

    public static <K, V> Map<K, V> fromList(NBTTagList list, Function<? super NBTTagCompound, ? extends K> keyFunction, Function<? super NBTTagCompound, ? extends V> valueFunction,
                                            Predicate<? super K> keyFilter, Predicate<? super V> valuePredicate) {
        Map<K, V> map = new HashMap<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            NBTTagCompound at = list.getCompound(i);
            K key = keyFunction.apply(at);
            V value = valueFunction.apply(at);
            if (keyFilter.test(key) && valuePredicate.test(value))
                map.put(key, value);
        }
        return map;
    }

    public static JsonObject fromBlockState(IBlockState state) {
        final JsonObject object = new JsonObject();
        object.addProperty("name", Objects.requireNonNull(state.getBlock().getRegistryName()).toString());
        final JsonObject properties = new JsonObject();
        state.getValues().entrySet().stream()
            .map(valuesBi(NBTBuilder::getPropertyName))
            .map(keys(IProperty::getName))
            .forEach(entry(properties::addProperty));
        object.add("properties", properties);
        return object;
    }

    public static Optional<IBlockState> getStateFromJson(JsonObject object) {
        return Optional.ofNullable(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(JsonUtils.getString(object, "name"))))
            .filter(Predicate.isEqual(Blocks.AIR).negate())
            .map(Block::getDefaultState)
            .map(iBlockState -> {
                IBlockState state = iBlockState;
                final JsonObject properties = JsonUtils.getJsonObject(object, "properties");
                final Map<String, IProperty<?>> map = iBlockState.getProperties().stream()
                    .map(toEntry(IProperty::getName, Function.identity()))
                    .collect(entryToMap());
                final List<? extends Map.Entry<? extends IProperty<?>, ?>> collect = properties.entrySet().stream()
                    .map(values(JsonElement::getAsString))
                    .map(keys(map::get)).filter(byKey(Objects::nonNull))
                    .map(valuesBi(IProperty::parseValue))
                    .flatMap(e -> stream(e.getValue()).map(toEntry(o -> e.getKey(), Function.identity())))
                    .collect(Collectors.toList());
                for (Map.Entry<? extends IProperty<?>, ?> e : collect) {
                    state = setValue(state, e.getKey(), e.getValue());
                }
                return state;
            });
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> String getPropertyName(IProperty<T> property, Comparable<?> entry) {
        return property.getName((T) entry);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> IBlockState setValue(IBlockState state, IProperty<T> property, Object entry) {
        return state.with(property, (T) entry);
    }

    private Map<String, T> map = new LinkedHashMap<>();
    private int index = 0;

    public NBTBuilder<T> setTag(Map.Entry<String, T> entry) {
        return setTag(entry.getKey(), entry.getValue());
    }

    public NBTBuilder<T> setTag(String key, T value) {
        map.put(key, value);
        return this;
    }

    public NBTBuilder<T> appendTag(T value) {
        map.put(String.valueOf(index), value);
        index++;
        return this;
    }

    public static <T extends INBTBase> NBTBuilder<T> appendAll(NBTBuilder<T> b1, NBTBuilder<T> b2) {
        b1.map.putAll(b2.map);
        return b1;
    }

    public static NBTBuilder<INBTBase> empty() {
        return new NBTBuilder<>();
    }

    public NBTTagCompound toTag() {
        NBTTagCompound tag = new NBTTagCompound();
        map.forEach(tag::setTag);
        return tag;
    }

    public NBTTagList toList() {
        NBTTagList list = new NBTTagList();
        list.addAll(map.values());
        return list;
    }
}
