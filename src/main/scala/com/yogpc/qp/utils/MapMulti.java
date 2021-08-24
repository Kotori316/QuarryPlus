package com.yogpc.qp.utils;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MapMulti {
    public static <FROM, TO> BiConsumer<FROM, Consumer<TO>> cast(Class<TO> toClass) {
        Objects.requireNonNull(toClass);
        return (from, toConsumer) -> {
            if (toClass.isInstance(from)) {
                toConsumer.accept(toClass.cast(from));
            }
        };
    }
}
