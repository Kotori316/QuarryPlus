package com.yogpc.qp.machines;

import java.util.Iterator;

public abstract class PickIterator<T> implements Iterator<T> {

    protected T current;

    /**
     * Update this iterator and set current to new one.
     *
     * @return the next element.
     */
    protected abstract T update();

    @Override
    public final T next() {
        var c = current;
        current = update();
        return c;
    }

    public abstract T head();

    public void reset() {
        this.current = head();
    }

    public final T peek() {
        return current;
    }

    public void setCurrent(T current) {
        this.current = current;
    }

}
