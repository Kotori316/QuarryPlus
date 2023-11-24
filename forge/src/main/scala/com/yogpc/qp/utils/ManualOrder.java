package com.yogpc.qp.utils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("ClassCanBeRecord") // to make fields un-accessible.
public class ManualOrder<T> implements Comparator<T> {
    private final Comparator<T> parent;
    private final Map<T, Integer> manualOrdering;

    public ManualOrder(Comparator<T> parent, Map<T, Integer> manualOrdering) {
        this.parent = parent;
        this.manualOrdering = Map.copyOf(manualOrdering);
    }

    @Override
    public int compare(T o1, T o2) {
        var index1 = manualOrdering.get(o1);
        var index2 = manualOrdering.get(o2);
        if (index1 == null) {
            // o1 is not in the list.
            if (index2 == null) {
                // o2 is also not in the list. Use normal ordering.
                return parent.compare(o1, o2);
            } else {
                // o2 is in the list. o2 is smaller than o1. o1 > o2
                return 1;
            }
        } else {
            // o1 is in the list.
            if (index2 == null) {
                // o2 isn't in the list. o1 < o2
                return -1;
            } else {
                return index1.compareTo(index2);
            }
        }
    }

    public static <T> Builder<T> builder(Comparator<T> parent) {
        return new Builder<>(parent);
    }

    public static class Builder<T> {
        private final Comparator<T> parent;
        private final Map<T, Integer> ordering = new HashMap<>();

        private Builder(Comparator<T> parent) {
            this.parent = parent;
        }

        public ManualOrder<T> build() {
            return new ManualOrder<>(parent, ordering);
        }

        public Builder<T> add(T t, int order) {
            ordering.put(t, order);
            return this;
        }

        public Builder<T> add(T t) {
            return add(t, ordering.values().stream().max(Comparator.naturalOrder()).orElse(0) + 1);
        }
    }
}
