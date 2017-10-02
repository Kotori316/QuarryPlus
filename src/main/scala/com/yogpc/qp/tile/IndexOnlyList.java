package com.yogpc.qp.tile;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiPredicate;

import javax.annotation.Nonnull;

public class IndexOnlyList<T> {

    private final List<T> tList;
    private final List<BiPredicate<T, Object>> predicates;
    private final int defaultIndex;
    private final Object mutex;

    public IndexOnlyList(@Nonnull List<T> tList, int defaultIndex, @Nonnull Object mutex) {
        this.tList = tList;
        this.defaultIndex = defaultIndex;
        this.mutex = mutex;
        BiPredicate<T, Object> equals = Object::equals;
        predicates = new LinkedList<>();
        predicates.add(equals);
    }

    public IndexOnlyList(@Nonnull List<T> tList, @Nonnull Object mutex) {
        this(tList, -1, mutex);
    }

    public int indexOf(Object o) {
        synchronized (mutex) {
            for (int i = 0; i < tList.size(); i++) {
                T item = tList.get(i);
                if (predicates.stream().anyMatch(biPredicate -> biPredicate.test(item, o))) {
                    return i;
                }
            }
            return defaultIndex;
        }
    }

    public boolean contains(Object o) {
        synchronized (mutex) {
            for (T item : tList) {
                if (predicates.stream().anyMatch(biPredicate -> biPredicate.test(item, o))) {
                    return true;
                }
            }
            return false;
        }
    }

    public void addPredicate(BiPredicate<T, Object> predicate) {
        predicates.add(predicate);
    }
}
