package com.yogpc.qp.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class NoDuplicateList<V extends Comparable<V>> implements List<V>, Set<V> {

    private final Set<V> set = new HashSet<>();
    private final List<V> list;

    public static <V extends Comparable<V>> NoDuplicateList<V> create(Supplier<List<V>> listSupplier) {
        return new NoDuplicateList<>(listSupplier);
    }

    public NoDuplicateList(Supplier<List<V>> listSupplier) {
        list = listSupplier.get();
    }

    @Override
    public int size() {
        return set.size();
    }

    @Override
    public boolean isEmpty() {
        return set.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return set.contains(o);
    }

    @Override
    public Iterator<V> iterator() {
        return listIterator();
    }

    @Override
    public void forEach(Consumer<? super V> action) {
        list.forEach(action);
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @SuppressWarnings("SuspiciousToArrayCall")
    @Override
    public <T> T[] toArray(T[] a) {
        return list.toArray(a);
    }

    @Override
    public boolean add(V v) {
        if (set.add(v)) {
            list.add(v);
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(Object o) {
        if (set.remove(o)) {
            list.remove(o);
            return true;
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return set.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends V> c) {
        boolean b = false;
        for (V v : c) {
            b |= add(v);
        }
        return b;
    }

    @Override
    public boolean addAll(int index, Collection<? extends V> c) {
        boolean b = false;
        for (V v : c) {
            if (add(index, v, null)) {
                index += 1;
                b = true;
            }
        }
        return b;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        set.removeAll(c);
        return list.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        set.retainAll(c);
        return list.retainAll(c);
    }

    @Override
    public void clear() {
        set.clear();
        list.clear();
    }

    @Override
    public V get(int index) {
        return list.get(index);
    }

    @Override
    public V set(int index, V element) {
        if (set.add(element)) {
            V v = list.set(index, element);
            set.remove(v);
            return v;
        }
        return null;
    }

    @Override
    public void add(int index, V element) {
        add(index, element, null);
    }

    @SuppressWarnings({"unused", "SameParameterValue"})
    private boolean add(int index, V element, Void dummy) {
        if (set.add(element)) {
            list.add(index, element);
            return true;
        }
        return false;
    }

    @Override
    public V remove(int index) {
        V v = list.remove(index);
        set.remove(v);
        return v;
    }

    @Override
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    @Override
    public ListIterator<V> listIterator() {
        return list.listIterator();
    }

    @Override
    public ListIterator<V> listIterator(int index) {
        return list.listIterator(index);
    }

    @Override
    public List<V> subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }

    @Override
    public Spliterator<V> spliterator() {
        return list.spliterator();
    }
}
