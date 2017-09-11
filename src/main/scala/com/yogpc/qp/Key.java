package com.yogpc.qp;

@SuppressWarnings("NonFinalFieldInEnum")
public enum Key {
    forward, mode("key.hover", 50), jump;
    public Object binding;
    public final String name;
    public final int id;

    Key() {
        this(null, 0);
    }

    Key(final String n, final int i) {
        this.name = n;
        this.id = i;
    }
}
