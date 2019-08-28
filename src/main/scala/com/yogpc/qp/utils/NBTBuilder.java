package com.yogpc.qp.utils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.state.IProperty;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import static com.google.common.collect.Streams.stream;
import static jp.t2v.lab.syntax.MapStreamSyntax.byKey;
import static jp.t2v.lab.syntax.MapStreamSyntax.entry;
import static jp.t2v.lab.syntax.MapStreamSyntax.entryToMap;
import static jp.t2v.lab.syntax.MapStreamSyntax.keys;
import static jp.t2v.lab.syntax.MapStreamSyntax.toAny;
import static jp.t2v.lab.syntax.MapStreamSyntax.toEntry;
import static jp.t2v.lab.syntax.MapStreamSyntax.values;
import static jp.t2v.lab.syntax.MapStreamSyntax.valuesBi;

public class NBTBuilder<T extends INBT> {

    @SuppressWarnings("RedundantCast")
    public static <K, V> ListNBT fromMap(Map<? extends K, ? extends V> map, String keyName, String valueName,
                                         Function<? super K, ? extends INBT> keyFunction, Function<? super V, ? extends INBT> valueFunction) {
        return map.entrySet().stream()
            .map(toEntry(keyFunction.compose(Map.Entry::getKey), valueFunction.compose(Map.Entry::getValue)))
            .map(toAny((k, v) -> {
                CompoundNBT compound = new CompoundNBT();
                compound.put(keyName, ((INBT) k));
                compound.put(valueName, ((INBT) v));
                return compound;
            }))
            .collect(Collectors.toCollection(ListNBT::new));
    }

    public static <K, V> Map<K, V> fromList(ListNBT list, Function<? super CompoundNBT, ? extends K> keyFunction, Function<? super CompoundNBT, ? extends V> valueFunction,
                                            Predicate<? super K> keyFilter, Predicate<? super V> valuePredicate) {
        Map<K, V> map = new HashMap<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT at = list.getCompound(i);
            K key = keyFunction.apply(at);
            V value = valueFunction.apply(at);
            if (keyFilter.test(key) && valuePredicate.test(value))
                map.put(key, value);
        }
        return map;
    }

    public static JsonObject fromBlockState(BlockState state) {
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

    public static Optional<BlockState> getStateFromJson(JsonObject object) {
        return Optional.ofNullable(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(JSONUtils.getString(object, "name"))))
            .filter(Predicate.isEqual(Blocks.AIR).negate())
            .map(Block::getDefaultState)
            .map(iBlockState -> {
                BlockState state = iBlockState;
                final JsonObject properties = JSONUtils.getJsonObject(object, "properties");
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
    private static <T extends Comparable<T>> BlockState setValue(BlockState state, IProperty<T> property, Object entry) {
        return state.with(property, (T) entry);
    }

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

    public static NBTBuilder<INBT> empty() {
        return new NBTBuilder<>();
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
