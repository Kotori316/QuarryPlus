package com.yogpc.qp.utils;

import java.util.HashMap;
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
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import static jp.t2v.lab.syntax.MapStreamSyntax.byKey;
import static jp.t2v.lab.syntax.MapStreamSyntax.entry;
import static jp.t2v.lab.syntax.MapStreamSyntax.entryToMap;
import static jp.t2v.lab.syntax.MapStreamSyntax.keys;
import static jp.t2v.lab.syntax.MapStreamSyntax.toEntry;
import static jp.t2v.lab.syntax.MapStreamSyntax.values;
import static jp.t2v.lab.syntax.MapStreamSyntax.valuesBi;

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

    public static JsonObject fromBlockState(IBlockState state) {
        final JsonObject object = new JsonObject();
        object.addProperty("name", Objects.requireNonNull(state.getBlock().getRegistryName()).toString());
        final JsonObject properties = new JsonObject();
        state.getProperties().entrySet().stream()
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
                final Map<String, IProperty<?>> map = iBlockState.getPropertyKeys().stream()
                    .map(toEntry(IProperty::getName, Function.identity()))
                    .collect(entryToMap());
                final List<? extends Map.Entry<? extends IProperty<?>, ?>> collect = properties.entrySet().stream()
                    .map(values(JsonElement::getAsString))
                    .map(keys(map::get)).filter(byKey(Objects::nonNull))
                    .map(valuesBi(IProperty::parseValue))
                    .flatMap(e -> e.getValue().asSet().stream().map(toEntry(o -> e.getKey(), Function.identity())))
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
        return state.withProperty(property, (T) entry);
    }
}
