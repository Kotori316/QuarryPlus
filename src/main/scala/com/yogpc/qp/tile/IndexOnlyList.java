package com.yogpc.qp.tile;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

public class IndexOnlyList<T> {

    private final List<T> tList;
    private final List<TwoPredicate<T>> predicates;
    private final int defaultIndex;
    private final Object mutex;

    public IndexOnlyList(@Nonnull List<T> tList, int defaultIndex, @Nonnull Object mutex) {
        this.tList = tList;
        this.defaultIndex = defaultIndex;
        this.mutex = mutex;
        TwoPredicate<T> twoPredicate = Object::equals;
        predicates = new LinkedList<>();
        predicates.add(twoPredicate);
    }

    public IndexOnlyList(@Nonnull List<T> tList, @Nonnull Object mutex) {
        this(tList, -1, mutex);
    }

    public int indexOf(Object o) {
        synchronized (mutex) {
            for (int i = 0; i < tList.size(); i++) {
                T item = tList.get(i);
                if (predicates.stream().anyMatch(tTwoPredicate -> tTwoPredicate.test(item, o))) {
                    return i;
                }
            }
            return defaultIndex;
        }
    }

    public boolean contains(Object o) {
        synchronized (mutex) {
            for (T item : tList) {
                if (predicates.stream().anyMatch(tTwoPredicate -> tTwoPredicate.test(item, o))) {
                    return true;
                }
            }
            return false;
        }
    }

    public void addPredicate(TwoPredicate<T> predicate) {
        predicates.add(predicate);
    }

    @FunctionalInterface
    public interface TwoPredicate<T> {
        boolean test(T t, Object r);
    }
}
