package com.yogpc.qp.utils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

public class LoopList<T> {

    private List<T> values = Collections.emptyList();
    private int size = 0;

    public T get(int index) {
        if (size == 0) {
            return null;
        }
        return values.get(getRealIndex(index));
    }

    /*
        public T getorDefault(int index, Supplier<T> supplier) {
            if (size == 0) return supplier.get();
            return values.get(getRealIndex(index));
        }
    */
    public Optional<T> getOptional(int index) {
        return Optional.ofNullable(get(index));
    }

    public void setList(@Nonnull List<T> list) {
        values = list;
        size = list.size();
    }

    public void add(T t) {
        values.add(t);
        size = values.size();
    }

    public void set(int index, T t) {
        values.set(getRealIndex(index), t);
        size = values.size();
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    public void remove(Object t) {
        if (values.remove(t)) {
            size = values.size();
        }
    }

    public void remove(int index) {
        values.remove(getRealIndex(index));
        size = values.size();
    }

    public int size() {
        return size;
    }

    private int getRealIndex(int i) {
        int a = i % size();
        if (a < 0) {
            return a + size();
        } else {
            return a;
        }
    }
}
