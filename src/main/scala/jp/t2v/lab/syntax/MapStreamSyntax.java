/*
 * MapStreamSyntax
 *
 * Copyright(c) gakuzzzz
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 *
 * Download : https://gist.github.com/gakuzzzz/9f35617943decf2893ea
 */
package jp.t2v.lab.syntax;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class MapStreamSyntax {

    public static <E, K, V> Function<E, Map.Entry<K, V>> toEntry(final Function<? super E, ? extends K> ketFactory, final Function<? super E, ? extends V> valueFactory) {
        return e -> new SimpleImmutableEntry<>(ketFactory.apply(e), valueFactory.apply(e));
    }

    public static <K1, V1, R> Function<Map.Entry<K1, V1>, R> toAny(final BiFunction<? super K1, ? super V1, ? extends R> f) {
        return e -> f.apply(e.getKey(), e.getValue());
    }

    public static <K1, K2, V> Function<Map.Entry<K1, V>, Map.Entry<K2, V>> keys(final Function<? super K1, ? extends K2> f) {
        return e -> new SimpleImmutableEntry<>(f.apply(e.getKey()), e.getValue());
    }

    public static <K, V, R> Function<Map.Entry<K, V>, R> keyToAny(final Function<? super K, ? extends R> f) {
        return e -> f.apply(e.getKey());
    }

    public static <K, V> Predicate<Map.Entry<K, V>> byKey(final Predicate<? super K> f) {
        return e -> f.test(e.getKey());
    }

    public static <K, V1, V2> Function<Map.Entry<K, V1>, Map.Entry<K, V2>> values(final Function<? super V1, ? extends V2> f) {
        return e -> new SimpleImmutableEntry<>(e.getKey(), f.apply(e.getValue()));
    }

    public static <K, V1, V2> IntFunction<Map.Entry<Integer, V2>> valuesInt(final IntFunction<? extends V2> f) {
        return e -> new SimpleImmutableEntry<>(e, f.apply(e));
    }

    public static <K, V, R> Function<Map.Entry<K, V>, R> valueToAny(final Function<? super V, ? extends R> f) {
        return e -> f.apply(e.getValue());
    }

    public static <K, V> Predicate<Map.Entry<K, V>> byValue(final Predicate<? super V> f) {
        return e -> f.test(e.getValue());
    }

    public static <K, V> Predicate<Map.Entry<K, V>> byEntry(final BiPredicate<? super K, ? super V> f) {
        return e -> f.test(e.getKey(), e.getValue());
    }

    public static <K, V> Collector<Map.Entry<K, V>, ?, Map<K, V>> entryToMap() {
        return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    public static <K, V> Collector<Map.Entry<K, V>, ?, Map<K, V>> entryToMap(final BinaryOperator<V> mergeFunction) {
        return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, mergeFunction);
    }

    public static <K, V, M extends Map<K, V>> Collector<Map.Entry<K, V>, ?, M> entryToMap(final BinaryOperator<V> mergeFunction, final Supplier<M> mapSupplier) {
        return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, mergeFunction, mapSupplier);
    }

    public static <K, V> Consumer<Map.Entry<K, V>> entry(BiConsumer<? super K, ? super V> biConsumer) {
        return kvEntry -> biConsumer.accept(kvEntry.getKey(), kvEntry.getValue());
    }
}
