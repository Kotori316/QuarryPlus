package com.yogpc.qp.machine;

import java.util.Iterator;

public abstract class PickIterator<T> implements Iterator<T> {
    protected T lastReturned = null;

    protected abstract T update();

    @Override
    public final T next() {
        return lastReturned = update();
    }

    public T getLastReturned() {
        return lastReturned;
    }

    public void setLastReturned(T lastReturned) {
        this.lastReturned = lastReturned;
    }
}
