package com.yogpc.qp.tile;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class IndexOnlyList<T> {

    private final ArrayList<T> tList;
    private final List<TwoPredicate<T>> predicates;
    private final int defaultIndex;

    public IndexOnlyList(ArrayList<T> tList, int defaultIndex) {
        this.tList = tList;
        this.defaultIndex = defaultIndex;
        TwoPredicate<T> twoPredicate = Object::equals;
        predicates = new LinkedList<>();
        predicates.add(twoPredicate);
    }

    public IndexOnlyList(ArrayList<T> tList) {
        this(tList, -1);
    }

    public int indexOf(Object o) {
        for (int i = 0; i < tList.size(); i++) {
            T item = tList.get(i);
            if (predicates.stream().anyMatch(tTwoPredicate -> tTwoPredicate.test(item, o))) {
                return i;
            }
        }
        return defaultIndex;
    }

    public boolean contains(Object o) {
        for (T item : tList) {
            if (predicates.stream().anyMatch(tTwoPredicate -> tTwoPredicate.test(item, o))) {
                return true;
            }
        }
        return false;
    }

    public void addPredicate(TwoPredicate<T> predicate) {
        predicates.add(predicate);
    }

    @FunctionalInterface
    public interface TwoPredicate<T> {
        boolean test(T t, Object r);
    }
}
